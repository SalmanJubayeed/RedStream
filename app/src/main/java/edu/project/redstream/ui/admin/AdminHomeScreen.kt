package edu.project.redstream.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import edu.project.redstream.data.model.BloodRequest
import edu.project.redstream.data.model.DonorApplication
import edu.project.redstream.data.model.User
import edu.project.redstream.ui.shared.toRelativeTime
import edu.project.redstream.viewmodel.AdminViewModel

// ── Palette ──────────────────────────────────────────────────────────────────
private val BgDark        = Color(0xFF0F0F0F)
private val Surface1      = Color(0xFF1A1A1A)
private val Surface2      = Color(0xFF242424)
private val RedPrimary    = Color(0xFFE53935)
private val RedLight      = Color(0xFFFF6B6B)
private val GreenApprove  = Color(0xFF43A047)
private val OrangeWarn    = Color(0xFFFB8C00)
private val TextPrimary   = Color(0xFFEEEEEE)
private val TextSecondary = Color(0xFF9E9E9E)
private val Divider       = Color(0xFF2A2A2A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Moderation", "Verify Users", "Donations")

    val pendingRequests    by viewModel.pendingRequests.collectAsState()
    val unverifiedDonors   by viewModel.unverifiedDonors.collectAsState()
    val approvedApps       by viewModel.approvedApplications.collectAsState()

    // Reload on tab switch
    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            0 -> viewModel.loadPendingRequests()
            1 -> viewModel.loadUnverifiedDonors()
            2 -> viewModel.loadApprovedApplications()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = RedPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Admin Panel",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        containerColor = BgDark
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Tab Row ───────────────────────────────────────────────────────
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor   = Surface1,
                contentColor     = RedPrimary,
                indicator        = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = RedPrimary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick  = { selectedTab = index },
                        text     = {
                            Text(
                                title,
                                color = if (selectedTab == index) RedPrimary else TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            // ── Tab Content ───────────────────────────────────────────────────
            when (selectedTab) {
                0 -> ModerationTab(
                    requests   = pendingRequests,
                    onApprove  = { viewModel.approveRequest(it) },
                    onHide     = { viewModel.hideRequest(it) }
                )
                1 -> VerifyDonorsTab(
                    donors   = unverifiedDonors,
                    onVerify = { viewModel.verifyDonor(it) },
                    onReject = { viewModel.rejectDonor(it) }
                )
                2 -> DonationsTab(
                    pairs     = approvedApps,
                    onConfirm = { requestId, donorUid ->
                        viewModel.confirmDonation(requestId, donorUid)
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TAB 1 — MODERATION
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ModerationTab(
    requests: List<BloodRequest>,
    onApprove: (String) -> Unit,
    onHide: (String) -> Unit
) {
    if (requests.isEmpty()) {
        AdminEmptyState(
            icon    = Icons.Default.CheckCircle,
            message = "No requests awaiting moderation",
            tint    = GreenApprove
        )
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SectionHeader(
                "Pending Review",
                "${requests.size} request${if (requests.size != 1) "s" else ""}"
            )
        }
        items(requests, key = { it.id.ifBlank { it.hospitalName + it.createdAt } }) { request ->
            ModerationCard(
                request   = request,
                onApprove = { onApprove(request.id) },
                onHide    = { onHide(request.id) }
            )
        }
    }
}

@Composable
private fun ModerationCard(
    request: BloodRequest,
    onApprove: () -> Unit,
    onHide: () -> Unit
) {
    var showHideDialog by remember { mutableStateOf(false) }

    Card(
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface1),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            // ── Header row ──────────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BloodGroupBadge(request.bloodGroupNeeded)
                UrgencyChip(request.urgency)
            }

            Spacer(Modifier.height(8.dp))

            // ── Hospital + phone ─────────────────────────────────────────────
            Text(
                request.hospitalName,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Text(
                request.contactPhone,
                color = TextSecondary,
                fontSize = 13.sp
            )

            if (request.notes.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    request.notes,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Meta row ────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    request.createdAt?.toRelativeTime() ?: "Just now",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Default.Bloodtype, null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    "${request.unitsNeeded} unit${if (request.unitsNeeded != 1) "s" else ""}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Divider)
            Spacer(Modifier.height(10.dp))

            // ── Action buttons ───────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showHideDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedLight),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(RedLight.copy(alpha = 0.4f))
                    )
                ) {
                    Icon(Icons.Default.VisibilityOff, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Hide", fontSize = 13.sp)
                }
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenApprove)
                ) {
                    Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Approve", fontSize = 13.sp)
                }
            }
        }
    }

    if (showHideDialog) {
        AlertDialog(
            onDismissRequest = { showHideDialog = false },
            containerColor   = Surface2,
            title = { Text("Hide Request?", color = TextPrimary) },
            text  = {
                Text(
                    "This request will be closed and hidden from donors. The recipient will not be notified.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onHide()
                    showHideDialog = false
                }) {
                    Text("Hide", color = RedLight)
                }
            },
            dismissButton = {
                TextButton(onClick = { showHideDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TAB 2 — VERIFY DONORS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun VerifyDonorsTab(
    donors: List<User>,
    onVerify: (String) -> Unit,
    onReject: (String) -> Unit
) {
    if (donors.isEmpty()) {
        AdminEmptyState(
            icon    = Icons.Default.VerifiedUser,
            message = "All donors are verified",
            tint    = GreenApprove
        )
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SectionHeader(
                "Awaiting Verification",
                "${donors.size} user${if (donors.size != 1) "s" else ""}"
            )
        }
        items(donors, key = { donor -> donors.indexOf(donor) }) { donor ->
            DonorVerificationCard(
                donor    = donor,
                onVerify = { onVerify(donor.uid) },
                onReject = { onReject(donor.uid) }
            )
        }
    }
}

@Composable
private fun DonorVerificationCard(
    donor: User,
    onVerify: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface1),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            // ── Avatar row ───────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(RedPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        donor.name.take(1).uppercase().ifBlank { "?" },
                        color = RedPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        donor.name.ifBlank { "Unknown" },
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        donor.email,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                if (donor.bloodGroup.isNotBlank()) {
                    BloodGroupBadge(donor.bloodGroup)
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Info chips ───────────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (donor.phone?.isNotBlank() == true) {
                    InfoChip(icon = Icons.Default.Phone, text = donor.phone)
                }
                if (donor.locationText.isNotBlank()) {
                    InfoChip(icon = Icons.Default.LocationOn, text = donor.locationText)
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                "Joined ${donor.createdAt?.toRelativeTime() ?: "recently"}",
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Divider)
            Spacer(Modifier.height(10.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(TextSecondary.copy(alpha = 0.3f))
                    )
                ) {
                    Text("Reject", fontSize = 13.sp)
                }
                Button(
                    onClick = onVerify,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenApprove)
                ) {
                    Icon(Icons.Default.VerifiedUser, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Verify", fontSize = 13.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TAB 3 — DONATION CONFIRMATIONS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DonationsTab(
    pairs: List<Pair<BloodRequest, DonorApplication>>,
    onConfirm: (requestId: String, donorUid: String) -> Unit
) {
    if (pairs.isEmpty()) {
        AdminEmptyState(
            icon    = Icons.Default.Favorite,
            message = "No pending donation confirmations",
            tint    = RedPrimary
        )
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SectionHeader(
                "Approved — Awaiting Confirmation",
                "${pairs.size} donation${if (pairs.size != 1) "s" else ""}"
            )
        }
        items(pairs, key = { "${it.first.id}_${it.second.donorId}" }) { (request, app) ->
            DonationConfirmCard(
                request   = request,
                app       = app,
                onConfirm = { onConfirm(request.id, app.donorId) }
            )
        }
    }
}

@Composable
private fun DonationConfirmCard(
    request: BloodRequest,
    app: DonorApplication,
    onConfirm: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    Card(
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface1),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Donation Approved",
                    color = GreenApprove,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                StatusBadge("APPROVED", GreenApprove)
            }

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    "Donor UID: ${app.donorId.take(12)}…",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Assignment, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    "Request ID: ${request.id.take(12)}…",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            if (app.donationDeadlineAt != null) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, null, tint = OrangeWarn, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Deadline: ${app.donationDeadlineAt.toRelativeTime()}",
                        color = OrangeWarn,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Divider)
            Spacer(Modifier.height(10.dp))

            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = RedPrimary)
            ) {
                Icon(Icons.Default.Favorite, null, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Confirm Donation")
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor   = Surface2,
            title = { Text("Confirm Donation?", color = TextPrimary) },
            text  = {
                Text(
                    "This will mark the donation as completed and close the blood request.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm()
                    showConfirmDialog = false
                }) {
                    Text("Confirm", color = GreenApprove)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  SHARED COMPONENTS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(Modifier.padding(bottom = 4.dp)) {
        Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(subtitle, color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun BloodGroupBadge(bloodGroup: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(RedPrimary.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            bloodGroup,
            color = RedPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun UrgencyChip(urgency: String) {
    val (bg, fg) = when (urgency.lowercase()) {
        "high"   -> RedPrimary.copy(alpha = 0.15f)   to RedLight
        "medium" -> OrangeWarn.copy(alpha = 0.15f)   to OrangeWarn
        else     -> GreenApprove.copy(alpha = 0.15f) to GreenApprove
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(urgency, color = fg, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatusBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Surface2)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, color = TextSecondary, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
private fun AdminEmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    tint: Color
) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                icon,
                contentDescription = null,
                tint = tint.copy(alpha = 0.5f),
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(14.dp))
            Text(message, color = TextSecondary, fontSize = 14.sp)
        }
    }
}