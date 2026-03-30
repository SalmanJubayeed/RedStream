package edu.project.redstream.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import edu.project.redstream.data.model.BloodRequest
import edu.project.redstream.data.model.DonorApplication
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val requestsCol = db.collection("requests")

    // ── Create ──────────────────────────────────────────────────────────────
    suspend fun createRequest(request: BloodRequest): String {
        val docRef = requestsCol.document()
        val withExpiry = request.copy(
            expiresAt = Timestamp(
                Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)
            )
        )
        docRef.set(withExpiry).await()
        return docRef.id
    }

    // ── Update request (edit) ────────────────────────────────────────────────
    suspend fun updateRequest(requestId: String, fields: Map<String, Any>) {
        requestsCol.document(requestId).update(fields).await()
    }

    // ── Delete request ───────────────────────────────────────────────────────
    suspend fun deleteRequest(requestId: String) {
        requestsCol.document(requestId).delete().await()
    }

    // ── Active requests for donor feed (not expired) ─────────────────────────
    fun getOpenRequestsFlow(bloodGroup: String? = null): Flow<List<BloodRequest>> =
        callbackFlow {
            var query: Query = requestsCol
                .whereEqualTo("status", "OPEN")
                .whereGreaterThan("expiresAt", Timestamp.now())
                .orderBy("expiresAt", Query.Direction.ASCENDING)

            if (!bloodGroup.isNullOrBlank())
                query = query.whereEqualTo("bloodGroupNeeded", bloodGroup)

            val listener = query.addSnapshotListener { snap, error ->
                if (error != null) {
                    android.util.Log.e("RequestRepo", "Feed error: ${error.message}")
                    return@addSnapshotListener
                }
                trySend(snap?.documents?.mapNotNull {
                    it.toObject(BloodRequest::class.java)
                } ?: emptyList())
            }
            awaitClose { listener.remove() }
        }

    // ── Recipient: ACTIVE requests (not expired) ─────────────────────────────
    fun getMyActiveRequestsFlow(recipientId: String): Flow<List<BloodRequest>> =
        callbackFlow {
            val listener = requestsCol
                .whereEqualTo("recipientId", recipientId)
                .whereEqualTo("status", "OPEN")
                .whereGreaterThan("expiresAt", Timestamp.now())
                .orderBy("expiresAt", Query.Direction.ASCENDING)
                .addSnapshotListener { snap, error ->
                    if (error != null) {
                        android.util.Log.e("RequestRepo", "ActiveRequests error: ${error.message}")
                        return@addSnapshotListener
                    }
                    trySend(snap?.documents?.mapNotNull {
                        it.toObject(BloodRequest::class.java)
                    } ?: emptyList())
                }
            awaitClose { listener.remove() }
        }

    // ── Recipient: ARCHIVED requests (expired or closed) ─────────────────────
    fun getMyArchivedRequestsFlow(recipientId: String): Flow<List<BloodRequest>> =
        callbackFlow {
            val listener = requestsCol
                .whereEqualTo("recipientId", recipientId)
                .whereLessThanOrEqualTo("expiresAt", Timestamp.now())
                .orderBy("expiresAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snap, error ->
                    if (error != null) {
                        android.util.Log.e("RequestRepo", "ArchivedRequests error: ${error.message}")
                        return@addSnapshotListener
                    }
                    trySend(snap?.documents?.mapNotNull {
                        it.toObject(BloodRequest::class.java)
                    } ?: emptyList())
                }
            awaitClose { listener.remove() }
        }

    // ── Keep old getMyRequestsFlow for backward compat ───────────────────────
    fun getMyRequestsFlow(recipientId: String): Flow<List<BloodRequest>> =
        getMyActiveRequestsFlow(recipientId)

    // ── Single request fetch ─────────────────────────────────────────────────
    suspend fun getRequest(requestId: String): BloodRequest? {
        return requestsCol.document(requestId)
            .get().await()
            .toObject(BloodRequest::class.java)
    }

    // ── Close a request ──────────────────────────────────────────────────────
    suspend fun closeRequest(requestId: String) {
        requestsCol.document(requestId).update("status", "CLOSED").await()
    }

    // ── Applications ─────────────────────────────────────────────────────────
    private fun appsCol(requestId: String) =
        requestsCol.document(requestId).collection("applications")

    suspend fun applyToRequest(requestId: String, application: DonorApplication) {
        appsCol(requestId).document(application.donorId).set(application).await()
    }

    fun getApplicationsFlow(requestId: String): Flow<List<DonorApplication>> =
        callbackFlow {
            val listener = appsCol(requestId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snap, _ ->
                    trySend(snap?.documents?.mapNotNull {
                        it.toObject(DonorApplication::class.java)
                    } ?: emptyList())
                }
            awaitClose { listener.remove() }
        }

    fun getMyApplicationsFlow(donorId: String): Flow<List<DonorApplication>> =
        callbackFlow {
            val listener = db.collectionGroup("applications")
                .whereEqualTo("donorId", donorId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snap, error ->
                    if (error != null) {
                        android.util.Log.e("RequestRepo", "Applications error: ${error.message}")
                        return@addSnapshotListener
                    }
                    trySend(snap?.documents?.mapNotNull {
                        it.toObject(DonorApplication::class.java)
                    } ?: emptyList())
                }
            awaitClose { listener.remove() }
        }

    suspend fun updateApplicationStatus(
        requestId: String,
        donorUid: String,
        status: String
    ) {
        // When approving, set donationDeadlineAt = now + neededByHours
        val request = getRequest(requestId)
        val deadlineMs = System.currentTimeMillis() +
                (request?.neededByHours ?: 24) * 60 * 60 * 1000L

        val fields = mutableMapOf<String, Any>(
            "status"      to status,
            "decision.by" to donorUid,
            "decision.at" to Timestamp.now()
        )
        if (status == "APPROVED") {
            fields["donationDeadlineAt"] = Timestamp(Date(deadlineMs))
            // Also set on parent request for donor countdown
            requestsCol.document(requestId)
                .update("donationDeadlineAt", Timestamp(Date(deadlineMs)))
                .await()
        }
        appsCol(requestId).document(donorUid).update(fields).await()
    }

    suspend fun hasApplied(requestId: String, donorId: String): Boolean {
        return appsCol(requestId).document(donorId).get().await().exists()
    }
}