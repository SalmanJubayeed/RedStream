package edu.project.redstream.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import edu.project.redstream.data.model.BloodRequest
import edu.project.redstream.data.model.DonorApplication
import edu.project.redstream.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val requestsCol = db.collection("requests")
    private val usersCol    = db.collection("users")

    // ── Moderation queue ─────────────────────────────────────────────────────
    // Returns OPEN requests that have no moderation decision yet
    // (moderation field is null or missing, or moderation.status == "pending")
    fun getModerationQueueFlow(): Flow<List<BloodRequest>> = callbackFlow {
        val listener = requestsCol
            .whereEqualTo("status", "OPEN")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    android.util.Log.e("AdminRepo", "Moderation queue error: ${error.message}")
                    return@addSnapshotListener
                }
                // Filter client-side: no moderation entry OR moderation.status != "approved"/"hidden"
                val unmoderated = snap?.documents?.mapNotNull {
                    it.toObject(BloodRequest::class.java)
                }?.filter { req ->
                    val modStatus = (req.moderation?.get("status") as? String)
                    modStatus == null || modStatus == "pending"
                } ?: emptyList()
                trySend(unmoderated)
            }
        awaitClose { listener.remove() }
    }

    // Approve = set moderation.status = "approved"
    // Hide    = set moderation.status = "hidden" AND status = "CLOSED"
    suspend fun moderateRequest(requestId: String, decision: String, reason: String) {
        val adminUid = auth.currentUser?.uid ?: ""
        val fields = mutableMapOf<String, Any>(
            "moderation.status"  to decision,
            "moderation.by"      to adminUid,
            "moderation.at"      to Timestamp.now()
        )
        if (reason.isNotBlank()) fields["moderation.reason"] = reason
        if (decision == "hidden") fields["status"] = "CLOSED"
        requestsCol.document(requestId).update(fields).await()
    }

    // ── Donor verification queue ─────────────────────────────────────────────
    // Returns donors (role == "donor") who are NOT yet verified
    fun getUnverifiedDonorsFlow(): Flow<List<User>> = callbackFlow {
        val listener = usersCol
            .whereEqualTo("role", "donor")
            .whereEqualTo("verified", false)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    android.util.Log.e("AdminRepo", "Donors queue error: ${error.message}")
                    return@addSnapshotListener
                }
                trySend(snap?.documents?.mapNotNull {
                    it.toObject(User::class.java)
                } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun setDonorVerified(uid: String, verified: Boolean) {
        usersCol.document(uid).update(
            mapOf(
                "verified"   to verified,
                "updatedAt"  to Timestamp.now()
            )
        ).await()
    }

    // ── Approved applications (donation confirmations) ────────────────────────
    // Collection group query: all applications with status == "APPROVED"
    fun getApprovedApplicationsFlow(): Flow<List<Pair<BloodRequest, DonorApplication>>> =
        callbackFlow {
            val listener = db.collectionGroup("applications")
                .whereEqualTo("status", "APPROVED")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snap, error ->
                    if (error != null) {
                        android.util.Log.e("AdminRepo", "Approved apps error: ${error.message}")
                        return@addSnapshotListener
                    }
                    val pairs = snap?.documents?.mapNotNull { doc ->
                        val app = doc.toObject(DonorApplication::class.java) ?: return@mapNotNull null
                        val requestId = doc.reference.parent.parent?.id ?: ""
                        BloodRequest(id = requestId) to app
                    } ?: emptyList()
                    trySend(pairs)
                }
            awaitClose { listener.remove() }
        }

    // Confirm a donation: mark request CLOSED, set moderation confirmed flag
    suspend fun confirmDonation(requestId: String, donorUid: String) {
        val adminUid = auth.currentUser?.uid ?: ""
        // 1. Close the request
        requestsCol.document(requestId).update(
            mapOf(
                "status"                  to "CLOSED",
                "moderation.donationConfirmedBy" to adminUid,
                "moderation.donationConfirmedAt" to Timestamp.now()
            )
        ).await()
        // 2. Update application to CONFIRMED
        requestsCol.document(requestId)
            .collection("applications")
            .document(donorUid)
            .update("status", "CONFIRMED")
            .await()
    }
}