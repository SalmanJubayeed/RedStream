package edu.project.redstream.ui.donor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.navigation.NavController
import edu.project.redstream.ui.Route

@Composable
fun DonorHomeScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF1A1A1A)) {
                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Favorite, null) }, label = { Text("Feed") })
                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.List, null) }, label = { Text("My Applications") })
                NavigationBarItem(selected = false,
                    onClick = { navController.navigate(Route.Profile.path) },
                    icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> Box(Modifier.padding(padding).fillMaxSize().background(Color(0xFF0F0F0F)),
                contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💉", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("No requests yet", color = Color.Gray)
                    Text("Week 2 will load requests here",
                        color = Color(0xFF555555), fontSize = 13.sp)
                }
            }
            1 -> Box(Modifier.padding(padding).fillMaxSize().background(Color(0xFF0F0F0F)),
                contentAlignment = Alignment.Center) {
                Text("No applications yet", color = Color.Gray)
            }
        }
    }
}