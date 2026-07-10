package com.example.floral.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floral.ui.theme.FloralTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                NavigationDrawerItem(
                    label = { Text("Profile") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        context.startActivity(Intent(context, ProfileActivity::class.java))
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        FirebaseAuth.getInstance().signOut()
                        Toast.makeText(context, "Logout Successful", Toast.LENGTH_SHORT).show()
                        context.startActivity(Intent(context, LoginActivity::class.java))
                        (context as? ComponentActivity)?.finish()
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                AdminCard(
                    title = "Manage Flowers",
                    icon = Icons.Default.Inventory
                ) {
                    context.startActivity(Intent(context, HomeActivity::class.java))
                }
                AdminCard(
                    title = "Manage Users",
                    icon = Icons.Default.People
                ) {
                    context.startActivity(Intent(context, ManageUsersActivity::class.java))
                }
                AdminCard(
                    title = "Manage Orders",
                    icon = Icons.Default.ShoppingCart
                ) {
                    context.startActivity(Intent(context, ManageOrdersActivity::class.java))
                }
            }
        }
    }
}

@Composable
fun AdminCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
