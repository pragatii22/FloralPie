package com.example.floral.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.floral.R
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
                ProfileBody(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileBody(onBack: () -> Unit, hideTopBar: Boolean = false) {
    val context = LocalContext.current
    val viewModel: UserViewModel = viewModel(factory = UserViewModelFactory())
    val userState by viewModel.users.observeAsState()
    
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var role by remember { mutableStateOf("user") }
    var email by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

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
            imageUrl = user.imageUrl
            role = user.role
            email = user.email
        }
    }

    ProfileContent(
        name = name,
        onNameChange = { name = it },
        address = address,
        onAddressChange = { address = it },
        contact = contact,
        onContactChange = { contact = it },
        email = email,
        imageUrl = imageUrl,
        selectedImageUri = selectedImageUri,
        loading = loading,
        onBack = onBack,
        onPickImage = { launcher.launch("image/*") },
        onSave = {
            if (name.isBlank() || address.isBlank() || contact.isBlank()) {
                Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            } else {
                loading = true
                val performUpdate = { finalImageUrl: String ->
                    currentUser?.uid?.let { uid ->
                        val updatedUser = UserModel(
                            id = uid,
                            name = name,
                            email = email,
                            address = address,
                            contact = contact,
                            role = role,
                            imageUrl = finalImageUrl
                        )
                        viewModel.editProfile(uid, updatedUser) { success, message ->
                            loading = false
                            if (success) {
                                viewModel.getUserId(uid)
                            }
                            Toast.makeText(context, if (success) "Profile updated successfully!" else message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                if (selectedImageUri != null) {
                    viewModel.uploadImage(selectedImageUri!!) { success, newUrl ->
                        if (success) {
                            performUpdate(newUrl)
                        } else {
                            loading = false
                            Toast.makeText(context, "Image upload failed: $newUrl", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    performUpdate(imageUrl)
                }
            }
        },
        onLogout = {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        },
        hideTopBar = hideTopBar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    name: String,
    onNameChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    contact: String,
    onContactChange: (String) -> Unit,
    email: String,
    imageUrl: String,
    selectedImageUri: Uri?,
    loading: Boolean,
    onBack: () -> Unit,
    onPickImage: () -> Unit,
    onSave: () -> Unit,
    onLogout: () -> Unit,
    hideTopBar: Boolean = false
) {
    Scaffold(
        topBar = {
            if (!hideTopBar) {
                TopAppBar(
                    title = { Text("Profile", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F8F8))
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Profile Picture
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                    .clickable { onPickImage() },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.logo)
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Placeholder",
                        modifier = Modifier.size(60.dp),
                        tint = Color.LightGray
                    )
                }
                
                // Small camera icon overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
            }

            Text(
                text = name.ifEmpty { "User Name" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp)
            )
            
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Personal Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))

                    ProfileInputField(value = name, onValueChange = onNameChange, label = "Full Name")
                    
                    ProfileInputField(value = email, onValueChange = {}, label = "Email", enabled = false)

                    ProfileInputField(value = address, onValueChange = onAddressChange, label = "Delivery Address")

                    ProfileInputField(value = contact, onValueChange = onContactChange, label = "Contact Number")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onLogout,
                modifier = Modifier.padding(bottom = 32.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ProfileInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean = true
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                disabledBorderColor = Color.LightGray.copy(alpha = 0.3f),
                disabledTextColor = Color.Gray
            ),
            singleLine = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfilePreview() {
    FloralTheme {
        ProfileContent(
            name = "Pragati gaire",
            onNameChange = {},
            address = "lazimpath sals pizza, ktm",
            onAddressChange = {},
            contact = "1234567890",
            onContactChange = {},
            email = "pragati@example.com",
            imageUrl = "",
            selectedImageUri = null,
            loading = false,
            onBack = {},
            onPickImage = {},
            onSave = {},
            onLogout = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileInputFieldPreview() {
    FloralTheme {
        ProfileInputField(
            value = "John Doe",
            onValueChange = {},
            label = "Full Name"
        )
    }
}

