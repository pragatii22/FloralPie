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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    var isEditing by remember { mutableStateOf(false) }

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

    val onLogout = {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

    if (isEditing) {
        EditProfileContent(
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
            onBack = { isEditing = false },
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
                                    isEditing = false
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
            }
        )
    } else {
        MainProfileContent(
            name = name,
            email = email,
            contact = contact,
            imageUrl = imageUrl,
            onBack = onBack,
            onProfileDetailsClick = { isEditing = true },
            onLogout = onLogout,
            hideTopBar = hideTopBar
        )
    }
}

@Composable
fun MainProfileContent(
    name: String,
    email: String,
    contact: String,
    imageUrl: String,
    onBack: () -> Unit,
    onProfileDetailsClick: () -> Unit,
    onLogout: () -> Unit,
    hideTopBar: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // Header with background and overlapping image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            // Purple Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .background(Color(0xFFD1C4E9)) // Light purple from image
            ) {
                if (!hideTopBar) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(top = 48.dp, start = 16.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.DarkGray)
                    }
                }
            }

            // Profile Image overlapping
            Surface(
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.BottomCenter)
                    .border(4.dp, Color.White, CircleShape),
                shape = CircleShape,
                shadowElevation = 8.dp
            ) {
                AsyncImage(
                    model = imageUrl.ifEmpty { R.drawable.logo },
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.logo)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name
        Text(
            text = name.ifEmpty { "User Name" },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Info Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            InfoRow(label = "Phone", value = contact.ifEmpty { "Not Provided" })
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow(label = "Mail", value = email)
        }

        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)

        // Options List
        ProfileOptionItem(
            icon = Icons.Default.Person,
            label = "Profile details",
            onClick = onProfileDetailsClick
        )
        ProfileOptionItem(
            icon = Icons.AutoMirrored.Filled.Logout,
            label = "Log out",
            onClick = onLogout
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.Gray, fontSize = 16.sp)
        Text(text = value, fontWeight = FontWeight.Medium, fontSize = 16.sp)
    }
}

@Composable
fun ProfileOptionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 16.dp, horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.DarkGray)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label, modifier = Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
        HorizontalDivider(modifier = Modifier.padding(start = 64.dp, end = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileContent(
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
    onSave: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
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

            Spacer(modifier = Modifier.height(32.dp))
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
        MainProfileContent(
            name = "Pragati Gaire",
            email = "pragati@gmail.com",
            contact = "0987654321",
            imageUrl = "",
            onBack = {},
            onProfileDetailsClick = {},
            onLogout = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfilePreview() {
    FloralTheme {
        EditProfileContent(
            name = "Pragati Gaire",
            onNameChange = {},
            address = "Lazimpath,ktm",
            onAddressChange = {},
            contact = "0987654321",
            onContactChange = {},
            email = "pragati@gmail.com",
            imageUrl = "",
            selectedImageUri = null,
            loading = false,
            onBack = {},
            onPickImage = {},
            onSave = {}
        )
    }
}
