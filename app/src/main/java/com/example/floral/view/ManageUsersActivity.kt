package com.example.floral.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.floral.model.UserModel
import com.example.floral.ui.theme.FloralTheme
import com.example.floral.viewmodel.UserViewModel
import com.example.floral.viewmodel.UserViewModelFactory

class ManageUsersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FloralTheme {
                ManageUsersBody { finish() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUsersBody(onBack: () -> Unit) {
    val viewModel: UserViewModel = viewModel(factory = UserViewModelFactory())
    val allUsers by viewModel.allUsers.observeAsState(emptyList())
    var userToDelete by remember { mutableStateOf<UserModel?>(null) }

    LaunchedEffect(Unit) {
        viewModel.getAllUser()
    }

    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Delete User") },
            text = { Text("Are you sure you want to delete this user?") },
            confirmButton = {
                TextButton(onClick = {
                    userToDelete?.let { user ->
                        viewModel.deleteUsr(user.id) { success, message ->
                            if (success) viewModel.getAllUser()
                        }
                    }
                    userToDelete = null
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Users", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allUsers ?: emptyList()) { user ->
                user?.let {
                    UserCard(user = it, onDelete = { userToDelete = it })
                }
            }
        }
    }
}

@Composable
fun UserCard(user: UserModel, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = user.email, color = Color.Gray)
                Text(text = user.role, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
    }
}
