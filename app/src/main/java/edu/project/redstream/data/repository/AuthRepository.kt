package edu.project.redstream.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {
    val currentUser: FirebaseUser? get() = auth.currentUser
    val isSignedIn: Boolean get() = auth.currentUser != null

    suspend fun signUp(email: String, password: String): String {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw IllegalStateException("UID null after sign up")
    }

    suspend fun signIn(email: String, password: String): String {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw IllegalStateException("UID null after sign in")
    }

    fun signOut() = auth.signOut()
}