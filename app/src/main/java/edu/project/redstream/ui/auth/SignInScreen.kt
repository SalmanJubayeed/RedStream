package edu.project.redstream.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.project.redstream.viewmodel.AuthUiState
import edu.project.redstream.viewmodel.AuthViewModel

@Composable
fun SignInScreen(
    viewModel: AuthViewModel,
    onGoToSignUp: () -> Unit
) {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState  by viewModel.uiState.collectAsState()

    Column(
        Modifier.fillMaxSize().background(Color(0xFF0F0F0F)).padding(24.dp)
    ) {
        Spacer(Modifier.height(60.dp))
        Text("Welcome Back", fontSize = 28.sp,
            color = Color.White, fontWeight = FontWeight.Bold)
        Text("Sign in to continue", fontSize = 14.sp, color = Color.Gray,
            modifier = Modifier.padding(bottom = 32.dp))

        OutlinedTextField(value = email, onValueChange = { email = it },
            label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = password, onValueChange = { password = it },
            label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            visualTransformation = PasswordVisualTransformation())

        if (uiState is AuthUiState.Error)
            Text((uiState as AuthUiState.Error).message,
                color = MaterialTheme.colorScheme.error, fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp))

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { viewModel.signIn(email, password) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = uiState !is AuthUiState.Loading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
        ) {
            if (uiState is AuthUiState.Loading)
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            else Text("Sign In", fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onGoToSignUp) {
            Text("Don't have an account? Sign Up", color = Color(0xFFEF5350))
        }
    }
}