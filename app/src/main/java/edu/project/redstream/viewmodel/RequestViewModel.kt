package edu.project.redstream.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.project.redstream.data.model.BloodRequest
import edu.project.redstream.data.model.DonorApplication
import edu.project.redstream.data.repository.RequestRepository
import edu.project.redstream.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RequestUiState {
    object Idle : RequestUiState()
    object Loading : RequestUiState()
    object Success : RequestUiState()
    data class Error(val message: String) : RequestUiState()
}

@HiltViewModel
class RequestViewModel @Inject constructor(
    private val requestRepo: RequestRepository,
    private val userRepo: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<RequestUiState>(RequestUiState.Idle)
    val uiState: StateFlow<RequestUiState> = _uiState.asStateFlow()

    val currentUid get() = auth.currentUser?.uid ?: ""

    // ── My active requests ───────────────────────────────────────────────────
    val myRequests = MutableStateFlow<List<BloodRequest>>(emptyList())

    // ── My archived requests ─────────────────────────────────────────────────
    val myArchivedRequests = MutableStateFlow<List<BloodRequest>>(emptyList())

    // ── Applicants with names resolved ──────────────────────────────────────
    val applicants = MutableStateFlow<List<DonorApplication>>(emptyList())
    val applicantNames = MutableStateFlow<Map<String, String>>(emptyMap())

    fun loadMyRequests() {
        Log.d("RequestVM", "loadMyRequests, uid: $currentUid")
        viewModelScope.launch {
            requestRepo.getMyActiveRequestsFlow(currentUid).collect { list ->
                Log.d("RequestVM", "active requests: ${list.size}")
                myRequests.value = list
            }
        }
    }

    fun loadMyArchivedRequests() {
        viewModelScope.launch {
            requestRepo.getMyArchivedRequestsFlow(currentUid).collect { list ->
                myArchivedRequests.value = list
            }
        }
    }

    // ── Create ───────────────────────────────────────────────────────────────
    fun createRequest(
        bloodGroup: String,
        units: Int,
        urgency: String,
        hospital: String,
        phone: String,
        notes: String,
        neededByHours: Int
    ) {
        if (bloodGroup.isBlank() || hospital.isBlank() || phone.isBlank()) {
            _uiState.value = RequestUiState.Error(
                "Blood group, hospital and phone are required"
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = RequestUiState.Loading
            val request = BloodRequest(
                recipientId      = currentUid,
                bloodGroupNeeded = bloodGroup,
                unitsNeeded      = units,
                urgency          = urgency,
                hospitalName     = hospital,
                contactPhone     = phone,
                notes            = notes,
                status           = "OPEN",
                neededByHours    = neededByHours
            )
            runCatching { requestRepo.createRequest(request) }
                .onSuccess { _uiState.value = RequestUiState.Success }
                .onFailure { e ->
                    _uiState.value =
                        RequestUiState.Error(e.message ?: "Failed to create request")
                }
        }
    }

    // ── Edit ─────────────────────────────────────────────────────────────────
    fun editRequest(
        requestId: String,
        bloodGroup: String,
        units: Int,
        urgency: String,
        hospital: String,
        phone: String,
        notes: String,
        neededByHours: Int
    ) {
        viewModelScope.launch {
            _uiState.value = RequestUiState.Loading
            runCatching {
                requestRepo.updateRequest(
                    requestId, mapOf(
                        "bloodGroupNeeded" to bloodGroup,
                        "unitsNeeded"      to units,
                        "urgency"          to urgency,
                        "hospitalName"     to hospital,
                        "contactPhone"     to phone,
                        "notes"            to notes,
                        "neededByHours"    to neededByHours
                    )
                )
            }
                .onSuccess { _uiState.value = RequestUiState.Success }
                .onFailure { e ->
                    _uiState.value =
                        RequestUiState.Error(e.message ?: "Failed to update request")
                }
        }
    }

    // ── Delete ───────────────────────────────────────────────────────────────
    fun deleteRequest(requestId: String) {
        viewModelScope.launch {
            runCatching { requestRepo.deleteRequest(requestId) }
                .onSuccess { Log.d("RequestVM", "Request deleted: $requestId") }
                .onFailure { e -> Log.e("RequestVM", "Delete failed: ${e.message}") }
        }
    }

    // ── Load applicants + resolve their names ────────────────────────────────
    fun loadApplicants(requestId: String) {
        viewModelScope.launch {
            requestRepo.getApplicationsFlow(requestId).collect { list ->
                applicants.value = list
                // Resolve donor names from users collection
                val names = mutableMapOf<String, String>()
                list.forEach { app ->
                    if (!names.containsKey(app.donorId)) {
                        names[app.donorId] = userRepo.getDonorName(app.donorId)
                    }
                }
                applicantNames.value = names
            }
        }
    }

    // ── Approve / Reject ─────────────────────────────────────────────────────
    fun updateApplicationStatus(requestId: String, donorUid: String, status: String) {
        viewModelScope.launch {
            runCatching {
                requestRepo.updateApplicationStatus(requestId, donorUid, status)
            }
        }
    }

    fun closeRequest(requestId: String) {
        viewModelScope.launch {
            runCatching { requestRepo.closeRequest(requestId) }
        }
    }

    suspend fun getRequestById(requestId: String): BloodRequest? {
        return runCatching { requestRepo.getRequest(requestId) }.getOrNull()
    }

    fun clearState() { _uiState.value = RequestUiState.Idle }
}