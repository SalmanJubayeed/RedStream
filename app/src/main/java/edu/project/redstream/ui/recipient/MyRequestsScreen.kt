package edu.project.redstream.ui.recipient

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import edu.project.redstream.ui.Route
import edu.project.redstream.ui.shared.toCountdown
import edu.project.redstream.ui.shared.toRelativeTime
import edu.project.redstream.viewmodel.RequestUiState
import edu.project.redstream.viewmodel.RequestViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRequestsScreen(
    navController: NavController,
    viewModel: RequestViewModel = hiltViewModel()
) {
    val activeRequests   by viewModel.myRequests.collectAsState()
    val archivedRequests by viewModel.myArchivedRequests.collectAsState()
    val uiState          by viewModel.uiState.collectAsState()
    var selectedTab      by remember { mutableStateOf(0) }
    var editingRequest   by remember { mutableStateOf<BloodRequest?>(null) }
    var showDeleteDialog by remember { mutableStateOf<BloodRequest?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadMyRequests()
        viewModel.loadMyArchivedRequests()
    }

    LaunchedEffect(uiState) {
        if (uiState is RequestUiState.Success) {
            editingRequest = null
            viewModel.clearState()
        }
    }

    // Edit dialog
    editingRequest?.let { req ->
        EditRequestDialog(
            request   = req,
            onDismiss = { editingRequest = null },
            onSave    = { bg, units, urgency, hospital, phone, notes, hours ->
                viewModel.editRequest(
                    req.id, bg, units, urgency, hospital, phone, notes, hours
                )
            }
        )
    }

    // Delete confirmation
    showDeleteDialog?.let { req ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Request?", color = Color.White) },
            text  = { Text("This cannot be undone.", color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRequest(req.id)
                    showDeleteDialog = null
                }) { Text("Delete", color = Color(0xFFEF5350)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1A1A1A)
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF1A1A1A)) {
                NavigationBarItem(
                    selected = true, onClick = {},
                    icon  = { Icon(Icons.Default.Add, null) },
                    label = { Text("Requests") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick  = { navController.navigate(Route.Profile.path) },
                    icon     = { Icon(Icons.Default.Person, null) },
                    label    = { Text("Profile") }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0)
                FloatingActionButton(
                    onClick        = { navController.navigate(Route.CreateRequest.path) },
                    containerColor = Color(0xFFC62828)
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White)
                }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFF0F0F0F))
                .padding(16.dp)
        ) {
            Text(
                "🏥 My Requests", fontSize = 22.sp,
                color = Color.White, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            // Active / Archive tab row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor   = Color(0xFF1A1A1A),
                contentColor     = Color(0xFFEF5350)
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text(
                        "Active (${activeRequests.size})",
                        modifier = Modifier.padding(vertical = 12.dp),
                        color    = if (selectedTab == 0) Color(0xFFEF5350) else Color.Gray
                    )
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text(
                        "Archive (${archivedRequests.size})",
                        modifier = Modifier.padding(vertical = 12.dp),
                        color    = if (selectedTab == 1) Color(0xFFEF5350) else Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            when (selectedTab) {
                0 -> {
                    if (activeRequests.isEmpty()) {
                        EmptyState("📋", "No active requests", "Tap + to create one")
                    } else {
                        // Latest first — list is already ordered by expiresAt ASC from
                        // Firestore, reverse so newest createdAt shows first
                        val sorted = activeRequests.sortedByDescending {
                            it.createdAt?.toDate()?.time ?: 0L
                        }
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(sorted) { request ->
                                RecipientRequestCard(
                                    request  = request,
                                    onClick  = {
                                        navController.navigate(
                                            Route.RequestDetail.withId(request.id)
                                        )
                                    },
                                    onEdit   = { editingRequest = request },
                                    onDelete = { showDeleteDialog = request }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    if (archivedRequests.isEmpty()) {
                        EmptyState("🗄️", "No archived requests",
                            "Expired requests appear here")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(archivedRequests) { request ->
                                ArchivedRequestCard(request = request)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Recipient request card — countdown + posted time + needed by ──────────────
@Composable
fun RecipientRequestCard(
    request: BloodRequest,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // Tick every second so countdown refreshes live
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) { delay(1000); tick++ }
    }

    val urgencyColor = when (request.urgency) {
        "High"   -> Color(0xFFC62828)
        "Medium" -> Color(0xFFF57C00)
        else     -> Color(0xFF2E7D32)
    }

    // Time remaining out of needed-by window
    val neededByMs      = request.neededByHours * 60 * 60 * 1000L
    val postedMs        = request.createdAt?.toDate()?.time ?: System.currentTimeMillis()
    val deadlineMs      = postedMs + neededByMs
    val remainingMs     = deadlineMs - System.currentTimeMillis()
    val isUrgent        = remainingMs in 0..3_600_000   // under 1 hour
    val isExpired       = remainingMs <= 0
    val neededByColor   = when {
        isExpired -> Color(0xFF888888)
        isUrgent  -> Color(0xFFEF5350)
        else      -> Color(0xFFFFA726)
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(Modifier.padding(16.dp)) {

            // Top row — blood group + edit/delete
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        Modifier
                            .background(Color(0xFF3B0E0E), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            request.bloodGroupNeeded,
                            color      = Color(0xFFEF5350),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 16.sp
                        )
                    }
                    Box(
                        Modifier
                            .background(
                                urgencyColor.copy(alpha = 0.15f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            request.urgency, color = urgencyColor,
                            fontSize = 11.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit, null,
                            tint     = Color(0xFF90CAF9),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete, null,
                            tint     = Color(0xFFEF5350),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                request.hospitalName, color = Color.White,
                fontWeight = FontWeight.SemiBold, fontSize = 14.sp
            )
            Text(
                "${request.unitsNeeded} unit(s) • ${request.contactPhone}",
                color = Color.Gray, fontSize = 12.sp
            )

            Spacer(Modifier.height(8.dp))

            // Posted time
            request.createdAt?.let {
                Text(
                    "Posted ${it.toRelativeTime()}",
                    color = Color(0xFF666666), fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(6.dp))

            // Needed-by countdown — "Donation needed within Xh Ym Zs"
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(
                        neededByColor.copy(alpha = 0.1f),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "🩸 Needed within",
                    color = neededByColor, fontSize = 11.sp
                )
                Text(
                    if (isExpired) "Time passed"
                    else {
                        val h = remainingMs / 3_600_000
                        val m = (remainingMs % 3_600_000) / 60_000
                        val s = (remainingMs % 60_000) / 1_000
                        when {
                            h > 0  -> "${h}h ${m}m ${s}s"
                            m > 0  -> "${m}m ${s}s"
                            else   -> "${s}s"
                        }
                    },
                    color      = neededByColor,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Donation deadline countdown (after a donor is approved)
            request.donationDeadlineAt?.let { deadline ->
                if (deadline.toDate().time > System.currentTimeMillis()) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1A1A3A), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "✅ Donor approved — due in",
                            color = Color(0xFF90CAF9), fontSize = 11.sp
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

// ── Archived card ─────────────────────────────────────────────────────────────
@Composable
fun ArchivedRequestCard(request: BloodRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = Color(0xFF141414))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    request.bloodGroupNeeded,
                    color      = Color(0xFF666666),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 16.sp
                )
                Box(
                    Modifier
                        .background(Color(0xFF2A2A2A), RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("Archived", color = Color(0xFF666666), fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(request.hospitalName, color = Color(0xFF888888), fontSize = 13.sp)
            request.createdAt?.let {
                Text(
                    "Posted ${it.toRelativeTime()}",
                    color = Color(0xFF555555), fontSize = 11.sp
                )
            }
        }
    }
}

// ── Edit dialog ───────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRequestDialog(
    request: BloodRequest,
    onDismiss: () -> Unit,
    onSave: (String, Int, String, String, String, String, Int) -> Unit
) {
    var bloodGroup    by remember { mutableStateOf(request.bloodGroupNeeded) }
    var units         by remember { mutableStateOf(request.unitsNeeded.toString()) }
    var urgency       by remember { mutableStateOf(request.urgency) }
    var hospital      by remember { mutableStateOf(request.hospitalName) }
    var phone         by remember { mutableStateOf(request.contactPhone) }
    var notes         by remember { mutableStateOf(request.notes) }
    var neededByHours by remember { mutableStateOf(request.neededByHours.toFloat()) }
    var bgExpanded    by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Request", color = Color.White) },
        text  = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                ExposedDropdownMenuBox(
                    expanded        = bgExpanded,
                    onExpandedChange = { bgExpanded = it }
                ) {
                    OutlinedTextField(
                        value    = bloodGroup, onValueChange = {},
                        readOnly = true,
                        label    = { Text("Blood Group") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(bgExpanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded        = bgExpanded,
                        onDismissRequest = { bgExpanded = false }
                    ) {
                        BLOOD_GROUPS.forEach { bg ->
                            DropdownMenuItem(
                                text    = { Text(bg) },
                                onClick = { bloodGroup = bg; bgExpanded = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value         = units,
                    onValueChange = { if (it.all(Char::isDigit)) units = it },
                    label         = { Text("Units") },
                    modifier      = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value         = hospital,
                    onValueChange = { hospital = it },
                    label         = { Text("Hospital") },
                    modifier      = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value         = phone,
                    onValueChange = { phone = it },
                    label         = { Text("Phone") },
                    modifier      = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value         = notes,
                    onValueChange = { notes = it },
                    label         = { Text("Notes") },
                    modifier      = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Text("Urgency", color = Color.Gray, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("High", "Medium", "Low").forEach { level ->
                        FilterChip(
                            selected = urgency == level,
                            onClick  = { urgency = level },
                            label    = { Text(level, fontSize = 12.sp) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Time Needed", color = Color.Gray, fontSize = 12.sp)
                    Text(
                        "${neededByHours.toInt()}h",
                        color      = Color(0xFFEF5350),
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value         = neededByHours,
                    onValueChange = { neededByHours = it },
                    valueRange    = 1f..24f,
                    steps         = 22,
                    colors        = SliderDefaults.colors(
                        thumbColor       = Color(0xFFEF5350),
                        activeTrackColor = Color(0xFFC62828)
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    bloodGroup, units.toIntOrNull() ?: 1,
                    urgency, hospital, phone, notes,
                    neededByHours.toInt()
                )
            }) { Text("Save", color = Color(0xFFEF5350)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF1A1A1A)
    )
}

// ── Empty state helper ────────────────────────────────────────────────────────
@Composable
fun EmptyState(emoji: String, title: String, subtitle: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text(title, color = Color.Gray)
            Text(subtitle, color = Color(0xFF555555), fontSize = 13.sp)
        }
    }
}

// ── RequestCard kept for backward compatibility with donor feed ───────────────
@Composable
fun RequestCard(request: BloodRequest, onClick: () -> Unit) {
    val urgencyColor = when (request.urgency) {
        "High"   -> Color(0xFFC62828)
        "Medium" -> Color(0xFFF57C00)
        else     -> Color(0xFF2E7D32)
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
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
                    Text(
                        request.bloodGroupNeeded, color = Color(0xFFEF5350),
                        fontWeight = FontWeight.ExtraBold, fontSize = 16.sp
                    )
                }
                Box(
                    Modifier
                        .background(
                            urgencyColor.copy(alpha = 0.15f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        request.urgency, color = urgencyColor,
                        fontSize = 11.sp, fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                request.hospitalName, color = Color.White,
                fontWeight = FontWeight.SemiBold, fontSize = 14.sp
            )
            Text(
                "${request.unitsNeeded} unit(s) needed",
                color = Color.Gray, fontSize = 13.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(request.contactPhone, color = Color(0xFF90CAF9), fontSize = 12.sp)
        }
    }
}