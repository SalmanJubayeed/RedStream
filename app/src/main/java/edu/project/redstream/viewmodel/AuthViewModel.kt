package edu.project.redstream.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.project.redstream.data.model.User
import edu.project.redstream.data.repository.AuthRepository
import edu.project.redstream.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val uid: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUid: String? get() = authRepo.currentUser?.uid

    fun signUp(email: String, password: String) {
        if (email.isBlank() || password.length < 6) {
            _uiState.value = AuthUiState.Error("Enter valid email and 6+ char password")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            runCatching { authRepo.signUp(email, password) }
                .onSuccess { uid ->
                    // Don't navigate yet — wait for role selection
                    // Just store the uid temporarily
                    _uiState.value = AuthUiState.Success(uid)
                }
                .onFailure { e ->
                    _uiState.value = AuthUiState.Error(e.message ?: "Sign up failed")
                }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            runCatching { authRepo.signIn(email, password) }
                .onSuccess { uid ->
                    if (userRepo.userExists(uid))
                        _uiState.value = AuthUiState.Success(uid)
                    else
                        _uiState.value = AuthUiState.Error("No profile found — please sign up")
                }
                .onFailure { e -> _uiState.value = AuthUiState.Error(e.message ?: "Sign in failed") }
        }
    }

    fun saveRoleAndProfile(
        uid: String, name: String, role: String,
        bloodGroup: String, location: String
    ) {
        if (name.isBlank() || location.isBlank()) {
            _uiState.value = AuthUiState.Error("Name and location are required")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val user = User(
                uid = uid,
                role = role,
                name = name,
                bloodGroup = bloodGroup,
                locationText = location,
                email = authRepo.currentUser?.email ?: ""
            )
            runCatching { userRepo.createUser(user) }
                .onSuccess {
                    // Confirm the write succeeded before navigating
                    val saved = userRepo.userExists(uid)
                    if (saved) {
                        _uiState.value = AuthUiState.Success(uid)
                    } else {
                        _uiState.value = AuthUiState.Error("Failed to save profile — check connection")
                    }
                }
                .onFailure { e ->
                    _uiState.value = AuthUiState.Error(e.message ?: "Profile save failed")
                }
        }
    }

    fun signOut() {
        authRepo.signOut()
        _uiState.value = AuthUiState.Idle
    }

    fun clearState() {
        _uiState.value = AuthUiState.Idle
    }
}