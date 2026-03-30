package edu.project.redstream.ui.recipient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import edu.project.redstream.data.model.BLOOD_GROUPS
import edu.project.redstream.viewmodel.RequestUiState
import edu.project.redstream.viewmodel.RequestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestScreen(
    navController: NavController,
    viewModel: RequestViewModel = hiltViewModel()
) {
    var bloodGroup    by remember { mutableStateOf("") }
    var units         by remember { mutableStateOf("1") }
    var urgency       by remember { mutableStateOf("High") }
    var hospital      by remember { mutableStateOf("") }
    var phone         by remember { mutableStateOf("") }
    var notes         by remember { mutableStateOf("") }
    var neededByHours by remember { mutableStateOf(6f) }
    var bgExpanded    by remember { mutableStateOf(false) }
    val uiState       by viewModel.uiState.collectAsState()
    val urgencyLevels = listOf("High", "Medium", "Low")

    LaunchedEffect(uiState) {
        if (uiState is RequestUiState.Success) {
            viewModel.clearState()
            navController.popBackStack()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top bar
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Text(
                "Create Blood Request", fontSize = 20.sp,
                color = Color.White, fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(16.dp))

        // Urgency selector
        Text("Urgency", color = Color.Gray, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            urgencyLevels.forEach { level ->
                val selected = urgency == level
                val color = when (level) {
                    "High"   -> Color(0xFFC62828)
                    "Medium" -> Color(0xFFF57C00)
                    else     -> Color(0xFF2E7D32)
                }
                OutlinedButton(
                    onClick = { urgency = level },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selected) color.copy(alpha = 0.2f)
                        else Color.Transparent
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (selected) color else Color(0xFF333333)
                    )
                ) {
                    Text(
                        level,
                        color = if (selected) color else Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Blood group dropdown
        Text("Blood Group Needed", color = Color.Gray, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        ExposedDropdownMenuBox(
            expanded = bgExpanded,
            onExpandedChange = { bgExpanded = it }
        ) {
            OutlinedTextField(
                value = bloodGroup,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select blood group") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(bgExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = bgExpanded,
                onDismissRequest = { bgExpanded = false }
            ) {
                BLOOD_GROUPS.forEach { bg ->
                    DropdownMenuItem(
                        text = { Text(bg) },
                        onClick = { bloodGroup = bg; bgExpanded = false }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Units needed
        Text("Units Needed", color = Color.Gray, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = units,
            onValueChange = { if (it.all(Char::isDigit)) units = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(Modifier.height(12.dp))

        // Hospital name
        Text("Hospital Name", color = Color.Gray, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = hospital,
            onValueChange = { hospital = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("e.g. Dhaka Medical College") }
        )

        Spacer(Modifier.height(12.dp))

        // Contact phone
        Text("Contact Phone", color = Color.Gray, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("+8801XXXXXXXXX") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        Spacer(Modifier.height(12.dp))

        // Notes
        Text("Notes (optional)", color = Color.Gray, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            placeholder = { Text("Any additional information...") },
            maxLines = 4
        )

        Spacer(Modifier.height(20.dp))

        // Time needed slider
        val hours = neededByHours.toInt()
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Time Needed", color = Color.Gray, fontSize = 13.sp)
            Text(
                if (hours < 24) "${hours}h" else "24h (max)",
                color = Color(0xFFEF5350),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = neededByHours,
            onValueChange = { neededByHours = it },
            valueRange = 1f..24f,
            steps = 22,
            colors = SliderDefaults.colors(
                thumbColor       = Color(0xFFEF5350),
                activeTrackColor = Color(0xFFC62828)
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("1h", color = Color(0xFF555555), fontSize = 11.sp)
            Text(
                "Donation needed within ${hours} hour(s)",
                color = Color(0xFF888888), fontSize = 11.sp
            )
            Text("24h", color = Color(0xFF555555), fontSize = 11.sp)
        }

        if (uiState is RequestUiState.Error)
            Text(
                (uiState as RequestUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.createRequest(
                    bloodGroup,
                    units.toIntOrNull() ?: 1,
                    urgency,
                    hospital,
                    phone,
                    notes,
                    neededByHours.toInt()
                )
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled  = uiState !is RequestUiState.Loading,
            colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
        ) {
            if (uiState is RequestUiState.Loading)
                CircularProgressIndicator(
                    color    = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            else
                Text("Post Request", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(Modifier.height(32.dp))
    }
}