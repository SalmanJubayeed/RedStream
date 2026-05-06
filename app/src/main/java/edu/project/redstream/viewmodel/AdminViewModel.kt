package edu.project.redstream.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.project.redstream.data.model.BloodRequest
import edu.project.redstream.data.model.DonorApplication
import edu.project.redstream.data.model.User
import edu.project.redstream.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AdminUiState {
    object Idle : AdminUiState()
    object Loading : AdminUiState()
    object Success : AdminUiState()
    data class Error(val message: String) : AdminUiState()
}

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepo: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Idle)
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    // ── Moderation queue: all OPEN requests (unmoderated) ───────────────────
    val pendingRequests = MutableStateFlow<List<BloodRequest>>(emptyList())

    // ── Donor verification queue ─────────────────────────────────────────────
    val unverifiedDonors = MutableStateFlow<List<User>>(emptyList())

    // ── Donation confirmations: approved applications ────────────────────────
    val approvedApplications = MutableStateFlow<List<Pair<BloodRequest, DonorApplication>>>(emptyList())

    init {
        loadPendingRequests()
        loadUnverifiedDonors()
        loadApprovedApplications()
    }

    // ── Moderation ───────────────────────────────────────────────────────────

    fun loadPendingRequests() {
        viewModelScope.launch {
            adminRepo.getModerationQueueFlow().collect { list ->
                pendingRequests.value = list
            }
        }
    }

    fun approveRequest(requestId: String) {
        viewModelScope.launch {
            runCatching {
                adminRepo.moderateRequest(requestId, "approved", "")
            }.onFailure { e ->
                Log.e("AdminVM", "Approve failed: ${e.message}")
                _uiState.value = AdminUiState.Error(e.message ?: "Failed to approve")
            }
        }
    }

    fun hideRequest(requestId: String) {
        viewModelScope.launch {
            runCatching {
                adminRepo.moderateRequest(requestId, "hidden", "")
            }.onFailure { e ->
                Log.e("AdminVM", "Hide failed: ${e.message}")
                _uiState.value = AdminUiState.Error(e.message ?: "Failed to hide")
            }
        }
    }

    // ── Donor Verification ───────────────────────────────────────────────────

    fun loadUnverifiedDonors() {
        viewModelScope.launch {
            adminRepo.getUnverifiedDonorsFlow().collect { list ->
                unverifiedDonors.value = list
            }
        }
    }

    fun verifyDonor(uid: String) {
        viewModelScope.launch {
            runCatching {
                adminRepo.setDonorVerified(uid, true)
            }.onSuccess {
                _uiState.value = AdminUiState.Success
            }.onFailure { e ->
                Log.e("AdminVM", "Verify failed: ${e.message}")
                _uiState.value = AdminUiState.Error(e.message ?: "Failed to verify")
            }
        }
    }

    fun rejectDonor(uid: String) {
        viewModelScope.launch {
            runCatching {
                adminRepo.setDonorVerified(uid, false)
            }.onFailure { e ->
                Log.e("AdminVM", "Reject failed: ${e.message}")
            }
        }
    }

    // ── Donation Confirmations ───────────────────────────────────────────────

    fun loadApprovedApplications() {
        viewModelScope.launch {
            adminRepo.getApprovedApplicationsFlow().collect { list ->
                approvedApplications.value = list
            }
        }
    }

    fun confirmDonation(requestId: String, donorUid: String) {
        viewModelScope.launch {
            runCatching {
                adminRepo.confirmDonation(requestId, donorUid)
            }.onSuccess {
                _uiState.value = AdminUiState.Success
            }.onFailure { e ->
                Log.e("AdminVM", "Confirm donation failed: ${e.message}")
                _uiState.value = AdminUiState.Error(e.message ?: "Failed to confirm")
            }
        }
    }

    fun clearState() { _uiState.value = AdminUiState.Idle }
}