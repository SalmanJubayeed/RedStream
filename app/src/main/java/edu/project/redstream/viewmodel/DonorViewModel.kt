package edu.project.redstream.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.project.redstream.data.model.BloodRequest
import edu.project.redstream.data.model.DonorApplication
import edu.project.redstream.data.repository.RequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DonorUiState {
    object Idle : DonorUiState()
    object Loading : DonorUiState()
    object Applied : DonorUiState()
    data class Error(val message: String) : DonorUiState()
}

@HiltViewModel
class DonorViewModel @Inject constructor(
    private val requestRepo: RequestRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<DonorUiState>(DonorUiState.Idle)
    val uiState: StateFlow<DonorUiState> = _uiState.asStateFlow()

    val currentUid get() = auth.currentUser?.uid ?: ""

    // ── Feed ────────────────────────────────────────────────────────────────
    val feedRequests = MutableStateFlow<List<BloodRequest>>(emptyList())

    private var bloodGroupFilter: String? = null
    private var urgencyFilter: String?    = null

    fun loadFeed(bloodGroup: String? = null) {
        bloodGroupFilter = bloodGroup
        viewModelScope.launch {
            requestRepo.getOpenRequestsFlow(bloodGroup).collect {
                feedRequests.value = it
            }
        }
    }

    // ── My applications ─────────────────────────────────────────────────────
    val myApplications = MutableStateFlow<List<DonorApplication>>(emptyList())

    fun loadMyApplications() {
        android.util.Log.d("DonorVM", "loadMyApplications uid: '$currentUid'")
        viewModelScope.launch {
            requestRepo.getMyApplicationsFlow(currentUid).collect { list ->
                android.util.Log.d("DonorVM", "applications count: ${list.size}")
                list.forEach {
                    android.util.Log.d("DonorVM", "app donorId: '${it.donorId}' status: ${it.status}")
                }
                myApplications.value = list
            }
        }
    }

    // ── Apply ───────────────────────────────────────────────────────────────
    fun applyToRequest(requestId: String, message: String) {
        viewModelScope.launch {
            _uiState.value = DonorUiState.Loading
            val alreadyApplied = requestRepo.hasApplied(requestId, currentUid)
            if (alreadyApplied) {
                _uiState.value = DonorUiState.Error("You already applied to this request")
                return@launch
            }
            val application = DonorApplication(
                donorId = currentUid,
                status  = "PENDING",
                message = message
            )
            runCatching { requestRepo.applyToRequest(requestId, application) }
                .onSuccess { _uiState.value = DonorUiState.Applied }
                .onFailure { e ->
                    _uiState.value = DonorUiState.Error(e.message ?: "Failed to apply")
                }
        }
    }

    fun clearState() { _uiState.value = DonorUiState.Idle }
}