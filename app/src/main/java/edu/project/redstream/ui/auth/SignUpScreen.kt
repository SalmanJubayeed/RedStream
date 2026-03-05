package edu.project.redstream.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.project.redstream.viewmodel.AuthUiState
import edu.project.redstream.viewmodel.AuthViewModel

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onSuccess: (String) -> Unit,
    onGoToSignIn: () -> Unit
) {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPw   by remember { mutableStateOf(false) }
    val uiState  by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success)
            onSuccess((uiState as AuthUiState.Success).uid)
    }

    Column(
        Modifier.fillMaxSize().background(Color(0xFF0F0F0F)).padding(24.dp)
    ) {
        Spacer(Modifier.height(60.dp))
        Text("Create Account", fontSize = 28.sp,
            color = Color.White, fontWeight = FontWeight.Bold)
        Text("Join RedStream today", fontSize = 14.sp, color = Color.Gray,
            modifier = Modifier.padding(bottom = 32.dp))

        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Password (min 6 chars)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (showPw) VisualTransformation.None
            else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showPw = !showPw }) {
                    Icon(
                        if (showPw) Icons.Default.VisibilityOff
                        else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            }
        )

        if (uiState is AuthUiState.Error)
            Text(
                (uiState as AuthUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { viewModel.signUp(email, password) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = uiState !is AuthUiState.Loading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
        ) {
            if (uiState is AuthUiState.Loading)
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            else
                Text("Sign Up", fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = onGoToSignIn) {
            Text("Already have an account? Sign In", color = Color(0xFFEF5350))
        }
    }
}