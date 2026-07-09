package com.example.floral.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.floral.model.UserModel
import com.example.floral.ui.theme.FloralTheme
import com.example.floral.viewmodel.UserViewModel
import com.example.floral.viewmodel.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FloralTheme {
                ProfileBody { finish() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileBody(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: UserViewModel = viewModel(factory = UserViewModelFactory())
    val userState by viewModel.users.observeAsState()
    
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("user") }
    var email by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    LaunchedEffect(Unit) {
        currentUser?.uid?.let { uid ->
            viewModel.getUserId(uid)
        }
    }

    LaunchedEffect(userState) {
        userState?.let { user ->
            name = user.name
            address = user.address
            contact = user.contact
            role = user.role
            email = user.email
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { },
                label = { Text("Email (Cannot be changed)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = contact,
                onValueChange = { contact = it },
                label = { Text("Contact") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (name.isBlank() || address.isBlank() || contact.isBlank()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    } else {
                        currentUser?.uid?.let { uid ->
                            val updatedUser = UserModel(
                                id = uid,
                                name = name,
                                email = email,
                                address = address,
                                contact = contact,
                                role = role
                            )
                            viewModel.editProfile(uid, updatedUser) { success, message ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                if (success) {
                                    onBack()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A5D8B))
            ) {
                Text("Update Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
