package edu.project.redstream.ui.recipient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import edu.project.redstream.ui.Route

@Composable
fun RecipientHomeScreen(navController: NavController) {
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF1A1A1A)) {
                NavigationBarItem(selected = true, onClick = {},
                    icon = { Icon(Icons.Default.Add, null) }, label = { Text("Requests") })
                NavigationBarItem(selected = false,
                    onClick = { navController.navigate(Route.Profile.path) },
                    icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()
            .background(Color(0xFF0F0F0F)).padding(16.dp)) {
            Text("🏥 My Requests", fontSize = 22.sp,
                color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { /* Week 2 */ },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))) {
                Icon(Icons.Default.Add, null, modifier = Modifier.padding(end = 8.dp))
                Text("Create Blood Request", fontWeight = FontWeight.Bold)
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No requests yet — create one above", color = Color.Gray)
            }
        }
    }
}