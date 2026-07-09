package com.example.floral.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floral.ui.theme.FloralTheme
import com.google.firebase.auth.FirebaseAuth

class AdminDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FloralTheme {
                AdminDashboardBody()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardBody() {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            AdminCard(title = "Manage Flowers") {
                context.startActivity(Intent(context, HomeActivity::class.java))
            }
            AdminCard(title = "Manage Users") {
                // context.startActivity(Intent(context, ManageUsersActivity::class.java))
                // Placeholder as not explicitly requested to implement Manage Users logic yet, 
                // but the button should be there.
            }
            AdminCard(title = "Profile") {
                 context.startActivity(Intent(context, ProfileActivity::class.java))
            }
            AdminCard(title = "Logout") {
                FirebaseAuth.getInstance().signOut()
                context.startActivity(Intent(context, LoginActivity::class.java))
                (context as? ComponentActivity)?.finish()
            }
        }
    }
}

@Composable
fun AdminCard(title: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        }
    }
}
