package edu.project.redstream.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import edu.project.redstream.ui.Route
import edu.project.redstream.viewmodel.AuthViewModel
import edu.project.redstream.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val user by profileViewModel.getUserFlow().collectAsState(initial = null)

    var showEditDialog by remember { mutableStateOf(false) }
    var editName       by remember { mutableStateOf("") }
    var editLocation   by remember { mutableStateOf("") }
    var editRole       by remember { mutableStateOf("donor") }

    // Sync fields when user data loads
    LaunchedEffect(user) {
        editName     = user?.name ?: ""
        editLocation = user?.locationText ?: ""
        editRole     = user?.role ?: "donor"
    }

    // ── Edit dialog ──────────────────────────────────────────────────────────
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile", color = Color.White) },
            text = {
                Column {
                    // Name
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    // Location
                    OutlinedTextField(
                        value = editLocation,
                        onValueChange = { editLocation = it },
                        label = { Text("Location") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))

                    // Role selector
                    Text("Role", color = Color.Gray, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("donor" to "🩸 Donor", "recipient" to "🏥 Recipient")
                            .forEach { (role, label) ->
                                OutlinedButton(
                                    onClick = { editRole = role },
                                    modifier = Modifier.weight(1f),
                                    border = BorderStroke(
                                        1.5.dp,
                                        if (editRole == role) Color(0xFFEF5350)
                                        else Color(0xFF333333)
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (editRole == role)
                                            Color(0x22EF5350) else Color.Transparent
                                    )
                                ) {
                                    Text(
                                        label,
                                        fontSize = 12.sp,
                                        color = if (editRole == role)
                                            Color(0xFFEF5350) else Color.Gray,
                                        fontWeight = if (editRole == role)
                                            FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        "⚠ Changing role will take effect on next sign in",
                        color = Color(0xFFFF8F00),
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    profileViewModel.updateProfile(editName, editLocation)
                    profileViewModel.updateRole(editRole)
                    showEditDialog = false
                }) {
                    Text("Save", color = Color(0xFFEF5350))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1A1A1A)
        )
    }

    // ── Main screen ──────────────────────────────────────────────────────────
    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back",
                    tint = Color.White)
            }
            Text("Profile", fontSize = 20.sp,
                color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(24.dp))

        // Avatar
        Box(
            Modifier
                .size(80.dp)
                .background(Color(0xFFC62828), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                user?.name?.firstOrNull()?.uppercase() ?: "?",
                fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(user?.name ?: "Loading...", fontSize = 20.sp,
            color = Color.White, fontWeight = FontWeight.Bold)
        Text(user?.email ?: "", color = Color.Gray, fontSize = 13.sp)

        Spacer(Modifier.height(32.dp))

        ProfileRow("🩸 Blood Group",
            user?.bloodGroup?.takeIf { it.isNotBlank() } ?: "—")
        ProfileRow("📍 Location",
            user?.locationText?.takeIf { it.isNotBlank() } ?: "—")
        ProfileRow("👤 Role",
            user?.role?.replaceFirstChar { it.uppercase() } ?: "—")
        ProfileRow("✅ Verified",
            if (user?.verified == true) "Yes" else "Pending")

        Spacer(Modifier.height(16.dp))

        // Edit button
        OutlinedButton(
            onClick = { showEditDialog = true },
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color(0xFF444444))
        ) {
            Text("Edit Profile", color = Color.White)
        }

        Spacer(Modifier.weight(1f))

        // Sign out
        OutlinedButton(
            onClick = {
                authViewModel.signOut()
                navController.navigate(Route.Welcome.path) {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color(0xFFEF5350))
        ) {
            Text("Sign Out", color = Color(0xFFEF5350))
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, color = Color.White, fontSize = 14.sp,
            fontWeight = FontWeight.Medium)
    }
    HorizontalDivider(color = Color(0xFF2E2E2E))
}