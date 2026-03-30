package edu.project.redstream.ui.recipient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import edu.project.redstream.data.model.BloodRequest
import edu.project.redstream.data.model.DonorApplication
import edu.project.redstream.viewmodel.DonorUiState
import edu.project.redstream.viewmodel.DonorViewModel
import edu.project.redstream.viewmodel.RequestViewModel

@Composable
fun RequestDetailScreen(
    navController: NavController,
    requestId: String,
    requestViewModel: RequestViewModel = hiltViewModel(),
    donorViewModel: DonorViewModel     = hiltViewModel()
) {
    val currentUid     = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val applicants     by requestViewModel.applicants.collectAsState()
    val donorState     by donorViewModel.uiState.collectAsState()
    val applicantNames by requestViewModel.applicantNames.collectAsState()

    var request         by remember { mutableStateOf<BloodRequest?>(null) }
    var applyMsg        by remember { mutableStateOf("") }
    var showApplyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(requestId) {
        requestViewModel.loadApplicants(requestId)
        request = requestViewModel.getRequestById(requestId)
    }

    val isRecipient = request?.recipientId == currentUid
    val isDonor     = !isRecipient && request != null

    LaunchedEffect(donorState) {
        if (donorState is DonorUiState.Applied) {
            donorViewModel.clearState()
            navController.popBackStack()
        }
    }

    // ── Apply dialog ─────────────────────────────────────────────────────────
    if (showApplyDialog) {
        AlertDialog(
            onDismissRequest = { showApplyDialog = false },
            title = { Text("Apply to Donate", color = Color.White) },
            text  = {
                Column {
                    Text("Leave a message for the recipient (optional)",
                        color = Color.Gray, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value         = applyMsg,
                        onValueChange = { applyMsg = it },
                        modifier      = Modifier.fillMaxWidth().height(100.dp),
                        placeholder   = { Text("e.g. I can donate tomorrow morning") },
                        maxLines      = 4
                    )
                    if (donorState is DonorUiState.Error)
                        Text(
                            (donorState as DonorUiState.Error).message,
                            color    = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    donorViewModel.applyToRequest(requestId, applyMsg)
                    showApplyDialog = false
                }) { Text("Confirm", color = Color(0xFFEF5350)) }
            },
            dismissButton = {
                TextButton(onClick = { showApplyDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1A1A1A)
        )
    }

    Column(
        Modifier.fillMaxSize().background(Color(0xFF0F0F0F)).padding(16.dp)
    ) {
        // Top bar
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Text("Request Detail", fontSize = 20.sp,
                color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        // Request info card
        if (request != null) {
            RequestInfoCard(request = request!!)
        } else {
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Box(
                    Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFEF5350))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Apply button — donors only
        if (isDonor) {
            Button(
                onClick = { showApplyDialog = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFC62828)
                )
            ) {
                Text("🩸 Apply to Donate", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
        }

        // Applicants list — recipients only
        if (isRecipient) {
            Text(
                "Applicants (${applicants.size})",
                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp
            )
            Spacer(Modifier.height(8.dp))

            if (applicants.isEmpty()) {
                Box(
                    Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👥", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("No applicants yet", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(applicants) { app ->
                        ApplicantCard(
                            application = app,
                            // ← donorName resolved from map, with fallback
                            donorName   = applicantNames[app.donorId]
                                ?: "Donor ${app.donorId.take(6)}",
                            onApprove   = {
                                requestViewModel.updateApplicationStatus(
                                    requestId, app.donorId, "APPROVED"
                                )
                            },
                            onReject    = {
                                requestViewModel.updateApplicationStatus(
                                    requestId, app.donorId, "REJECTED"
                                )
                            }
                        )
                    }
                }
            }
        }

        if (isDonor) {
            Text("Request Details", color = Color.White,
                fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Text("Tap Apply to Donate above to submit your application.",
                color = Color.Gray, fontSize = 13.sp)
        }
    }
}

@Composable
private fun RequestInfoCard(request: BloodRequest) {
    val urgencyColor = when (request.urgency) {
        "High"   -> Color(0xFFC62828)
        "Medium" -> Color(0xFFF57C00)
        else     -> Color(0xFF2E7D32)
    }
    Card(
        Modifier.fillMaxWidth(),
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .background(Color(0xFF3B0E0E), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(request.bloodGroupNeeded, color = Color(0xFFEF5350),
                        fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
                Box(
                    Modifier
                        .background(urgencyColor.copy(alpha = 0.15f),
                            RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(request.urgency, color = urgencyColor,
                        fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))
            DetailRow("🏥 Hospital", request.hospitalName)
            DetailRow("📦 Units",    "${request.unitsNeeded} unit(s)")
            DetailRow("📞 Contact",  request.contactPhone)
            DetailRow("⏱ Needed within", "${request.neededByHours}h")
            if (request.notes.isNotBlank())
                DetailRow("📝 Notes", request.notes)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text("$label: ", color = Color.Gray, fontSize = 13.sp)
        Text(value, color = Color.White, fontSize = 13.sp)
    }
}

// ── ApplicantCard — donorName is a normal parameter, not a default expression ──
@Composable
private fun ApplicantCard(
    application: DonorApplication,
    donorName: String,                    // ← plain parameter, no default expression
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val statusColor = when (application.status) {
        "APPROVED" -> Color(0xFF4CAF50)
        "REJECTED" -> Color(0xFFEF5350)
        else       -> Color(0xFFFFC107)
    }
    Card(
        Modifier.fillMaxWidth(),
        shape  = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF242424))
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar circle with first letter of donor name
                    Box(
                        Modifier
                            .size(36.dp)
                            .background(Color(0xFFC62828), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            donorName.firstOrNull()?.uppercase() ?: "D",
                            color = Color.White, fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    // ← now shows actual donor name
                    Text(
                        donorName,
                        color      = Color.White,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }   // ← Row closes here
                Box(
                    Modifier
                        .background(
                            statusColor.copy(alpha = 0.15f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        application.status,
                        color      = statusColor,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }   // ← outer Row closes here

            if (application.message.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text("\"${application.message}\"",
                    color = Color(0xFFCCCCCC), fontSize = 13.sp)
            }

            if (application.status == "PENDING") {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick  = onApprove,
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0x224CAF50)
                        )
                    ) {
                        Text("✓ Approve", color = Color(0xFF4CAF50), fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick  = onReject,
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0x22EF5350)
                        )
                    ) {
                        Text("✗ Reject", color = Color(0xFFEF5350), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
