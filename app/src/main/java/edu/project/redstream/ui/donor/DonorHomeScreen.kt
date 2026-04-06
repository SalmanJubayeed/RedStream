package edu.project.redstream.ui.donor

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
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
import edu.project.redstream.data.model.BLOOD_GROUPS
import edu.project.redstream.data.model.BloodRequest
import edu.project.redstream.data.model.DonorApplication
import edu.project.redstream.ui.Route
import edu.project.redstream.ui.recipient.RequestCard
import edu.project.redstream.ui.shared.toExpiryLabel
import edu.project.redstream.ui.shared.toRelativeTime
import edu.project.redstream.viewmodel.DonorViewModel
import androidx.compose.foundation.clickable
import edu.project.redstream.ui.shared.toCountdown


@Composable
fun DonorHomeScreen(
    navController: NavController,
    viewModel: DonorViewModel = hiltViewModel()
) {
    Log.d("DonorScreen", "DonorHomeScreen composing")

    var selectedTab      by remember { mutableStateOf(0) }
    var bloodGroupFilter by remember { mutableStateOf<String?>(null) }

    val feedRequests   by viewModel.feedRequests.collectAsState()
    val myApplications by viewModel.myApplications.collectAsState()

    LaunchedEffect(bloodGroupFilter) {
        viewModel.loadFeed(bloodGroupFilter)
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) {
            viewModel.loadMyApplications()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF1A1A1A)) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick  = { selectedTab = 0 },
                    icon     = { Icon(Icons.Default.Favorite, null) },
                    label    = { Text("Feed") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick  = { selectedTab = 1 },
                    icon     = { Icon(Icons.Default.List, null) },
                    label    = { Text("My Applications") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick  = { navController.navigate(Route.Profile.path) },
                    icon     = { Icon(Icons.Default.Person, null) },
                    label    = { Text("Profile") }
                )
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> FeedTab(
                modifier         = Modifier.padding(padding),
                requests         = feedRequests,
                bloodGroupFilter = bloodGroupFilter,
                onBloodGroup     = {
                    bloodGroupFilter =
                        if (bloodGroupFilter == it || it.isBlank()) null else it
                },
                onRequestClick = { id ->
                    navController.navigate(Route.RequestDetail.withId(id))
                }
            )
            1 -> ApplicationsTab(
                modifier     = Modifier.padding(padding),
                applications = myApplications
            )
        }
    }
}

@Composable
private fun FeedTab(
    modifier: Modifier,
    requests: List<BloodRequest>,
    bloodGroupFilter: String?,
    onBloodGroup: (String) -> Unit,
    onRequestClick: (String) -> Unit
) {
    Column(
        modifier.fillMaxSize().background(Color(0xFF0F0F0F)).padding(16.dp)
    ) {
        Text("🩸 Blood Requests", fontSize = 22.sp,
            color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        Text("Filter by blood group", color = Color.Gray, fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                FilterChip(
                    selected = bloodGroupFilter == null,
                    onClick  = { onBloodGroup("") },
                    label    = { Text("All", fontSize = 12.sp) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFC62828),
                        selectedLabelColor     = Color.White,
                        containerColor         = Color(0xFF1A1A1A),
                        labelColor             = Color.Gray
                    )
                )
            }
            items(BLOOD_GROUPS) { bg ->
                FilterChip(
                    selected = bloodGroupFilter == bg,
                    onClick  = { onBloodGroup(bg) },
                    label    = { Text(bg, fontSize = 12.sp) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFC62828),
                        selectedLabelColor     = Color.White,
                        containerColor         = Color(0xFF1A1A1A),
                        labelColor             = Color.Gray
                    )
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (requests.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💉", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("No requests found", color = Color.Gray)
                    if (bloodGroupFilter != null)
                        Text("No $bloodGroupFilter requests right now",
                            color = Color(0xFF555555), fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(requests) { request ->
                    // RequestCard now shows posted time + expiry
                    DonorFeedCard(
                        request  = request,
                        onClick  = { onRequestClick(request.id) }
                    )
                }
            }
        }
    }
}

// ── Donor feed card — shows posted time and expiry ────────────────────────────
@Composable
private fun DonorFeedCard(request: BloodRequest, onClick: () -> Unit) {
    val urgencyColor = when (request.urgency) {
        "High"   -> Color(0xFFC62828)
        "Medium" -> Color(0xFFF57C00)
        else     -> Color(0xFF2E7D32)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },        // ← clickable on Modifier, not on Card
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
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
                        fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
                Box(
                    Modifier
                        .background(
                            urgencyColor.copy(alpha = 0.15f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(request.urgency, color = urgencyColor,
                        fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(request.hospitalName, color = Color.White,
                fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text("${request.unitsNeeded} unit(s) needed",
                color = Color.Gray, fontSize = 13.sp)

            Spacer(Modifier.height(6.dp))

            // Posted time
            request.createdAt?.let {
                Text(
                    "Posted ${it.toRelativeTime()}",
                    color    = Color(0xFF666666),
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(4.dp))

            // Expiry label
            request.expiresAt?.let {
                Text(
                    it.toExpiryLabel(),
                    color    = Color(0xFF4CAF50),
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(6.dp))
            Text(request.contactPhone, color = Color(0xFF90CAF9), fontSize = 12.sp)
        }
    }
}

// ── Applications tab ──────────────────────────────────────────────────────────
@Composable
private fun ApplicationsTab(
    modifier: Modifier,
    applications: List<DonorApplication>
) {
    Column(
        modifier.fillMaxSize().background(Color(0xFF0F0F0F)).padding(16.dp)
    ) {
        Text(
            "📋 My Applications", fontSize = 22.sp,
            color = Color.White, fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        if (applications.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("No applications yet", color = Color.Gray)
                    Text(
                        "Apply to a request from the Feed tab",
                        color = Color(0xFF555555), fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(applications) { app ->
                    val statusColor = when (app.status) {
                        "APPROVED" -> Color(0xFF4CAF50)
                        "REJECTED" -> Color(0xFFEF5350)
                        else       -> Color(0xFFFFC107)
                    }
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A1A1A)
                        )
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                // Status badge
                                Box(
                                    Modifier
                                        .background(
                                            statusColor.copy(alpha = 0.15f),
                                            RoundedCornerShape(20.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        app.status,
                                        color      = statusColor,
                                        fontSize   = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(Modifier.height(6.dp))

                            // Message if any
                            if (app.message.isNotBlank())
                                Text(
                                    "\"${app.message}\"",
                                    color    = Color.Gray,
                                    fontSize = 12.sp
                                )

                            Spacer(Modifier.height(4.dp))

                            // Applied time
                            app.createdAt?.let {
                                Text(
                                    "Applied ${it.toRelativeTime()}",
                                    color    = Color(0xFF555555),
                                    fontSize = 11.sp
                                )
                            }
                            app.donationDeadlineAt?.let { deadline ->
                                val remaining = deadline.seconds * 1000L -
                                        System.currentTimeMillis()
                                if (remaining > 0) {
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Color(0xFF1A1A3A),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(
                                                horizontal = 10.dp,
                                                vertical   = 6.dp
                                            ),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "🩸 Donate within",
                                            color    = Color(0xFF90CAF9),
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            deadline.toCountdown(),
                                            color      = Color(0xFF90CAF9),
                                            fontSize   = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}