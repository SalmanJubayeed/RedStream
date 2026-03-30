package edu.project.redstream.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(onSignIn: () -> Unit, onSignUp: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🩸", fontSize = 72.sp)
        Spacer(Modifier.height(16.dp))
        Text("RedStream", fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold, color = Color(0xFFEF5350))
        Text("Blood donation, simplified.", fontSize = 15.sp,
            color = Color(0xFF888888),
            modifier = Modifier.padding(top = 8.dp, bottom = 48.dp))
        Button(
            onClick = onSignUp,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
        ) {
            Text("Create Account", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onSignIn,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Sign In", color = Color(0xFFEF5350), fontWeight = FontWeight.Bold)
        }
    }
}