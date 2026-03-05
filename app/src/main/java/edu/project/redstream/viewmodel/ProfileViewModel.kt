package edu.project.redstream.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.project.redstream.data.model.User
import edu.project.redstream.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    fun getUserFlow(): Flow<User?> {
        val uid = auth.currentUser?.uid ?: return emptyFlow()
        return userRepo.getUserFlow(uid)
    }

    // ← THIS is now INSIDE the class, not after it
    fun updateProfile(name: String, location: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            userRepo.updateProfile(
                uid, mapOf(
                    "name"         to name,
                    "locationText" to location
                )
            )
        }
    }
}