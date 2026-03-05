package edu.project.redstream.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import edu.project.redstream.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private fun userDoc(uid: String) = db.collection("users").document(uid)

    suspend fun createUser(user: User) {
        userDoc(user.uid).set(user).await()
    }

    suspend fun userExists(uid: String): Boolean {
        return userDoc(uid).get().await().exists()
    }

    suspend fun getUser(uid: String): User? {
        return userDoc(uid).get().await().toObject(User::class.java)
    }

    fun getUserFlow(uid: String): Flow<User?> = callbackFlow {
        val listener = userDoc(uid).addSnapshotListener { snap, _ ->
            trySend(snap?.toObject(User::class.java))
        }
        awaitClose { listener.remove() }
    }

    suspend fun updateProfile(uid: String, fields: Map<String, Any>) {
        userDoc(uid).update(fields).await()
    }
}