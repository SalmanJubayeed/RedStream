package edu.project.redstream.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.project.redstream.data.model.BLOOD_GROUPS
import edu.project.redstream.viewmodel.AuthUiState
import edu.project.redstream.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectScreen(uid: String, viewModel: AuthViewModel) {
    var selectedRole by remember { mutableStateOf("donor") }
    var name         by remember { mutableStateOf("") }
    var bloodGroup   by remember { mutableStateOf("A+") }
    var location     by remember { mutableStateOf("") }
    var bgExpanded   by remember { mutableStateOf(false) }
    val uiState      by viewModel.uiState.collectAsState()

    Column(
        Modifier.fillMaxSize().background(Color(0xFF0F0F0F))
            .padding(24.dp).verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(48.dp))
        Text("Set Up Profile", fontSize = 26.sp,
            color = Color.White, fontWeight = FontWeight.Bold)
        Text("Tell us about yourself", color = Color.Gray,
            modifier = Modifier.padding(bottom = 28.dp))

        Text("I am a...", color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("donor" to "🩸 Donor", "recipient" to "🏥 Recipient").forEach { (role, label) ->
                OutlinedButton(
                    onClick = { selectedRole = role },
                    modifier = Modifier.weight(1f).height(56.dp),
                    border = BorderStroke(2.dp,
                        if (selectedRole == role) Color(0xFFEF5350) else Color(0xFF333333)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedRole == role)
                            Color(0x22EF5350) else Color.Transparent)
                ) {
                    Text(label,
                        color = if (selectedRole == role) Color(0xFFEF5350) else Color.Gray,
                        fontWeight = if (selectedRole == role) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        OutlinedTextField(value = name, onValueChange = { name = it },
            label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(12.dp))

        ExposedDropdownMenuBox(expanded = bgExpanded, onExpandedChange = { bgExpanded = it }) {
            OutlinedTextField(value = bloodGroup, onValueChange = {}, readOnly = true,
                label = { Text("Blood Group") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(bgExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor())
            ExposedDropdownMenu(expanded = bgExpanded, onDismissRequest = { bgExpanded = false }) {
                BLOOD_GROUPS.forEach { bg ->
                    DropdownMenuItem(text = { Text(bg) },
                        onClick = { bloodGroup = bg; bgExpanded = false })
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = location, onValueChange = { location = it },
            label = { Text("City / District (e.g. Dhaka)") },
            modifier = Modifier.fillMaxWidth(), singleLine = true)

        if (uiState is AuthUiState.Error)
            Text((uiState as AuthUiState.Error).message,
                color = MaterialTheme.colorScheme.error, fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp))

        Spacer(Modifier.height(28.dp))
        Button(
            onClick = { viewModel.saveRoleAndProfile(uid, name, selectedRole, bloodGroup, location) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = name.isNotBlank() && location.isNotBlank() && uiState !is AuthUiState.Loading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
        ) {
            if (uiState is AuthUiState.Loading)
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            else Text("Continue →", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(32.dp))
    }
}