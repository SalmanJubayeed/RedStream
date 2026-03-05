package edu.project.redstream.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun AdminHomeScreen(navController: NavController) {
    Box(Modifier.fillMaxSize().background(Color(0xFF0F0F0F)),
        contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🛡️", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text("Admin Panel", color = Color.White, fontSize = 20.sp)
            Text("Week 3 feature", color = Color.Gray)
        }
    }
}