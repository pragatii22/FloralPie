package com.example.floral.view

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.floral.model.ProductModel
import com.example.floral.repo.ProductRepoImpl
import com.example.floral.ui.theme.FloralTheme
import com.example.floral.viewmodel.ProductViewModel

class EditProductActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val productId = intent.getStringExtra("productId") ?: ""
        setContent {
            FloralTheme {
                EditProductBody(productId)
            }
        }
    }
}

@Composable
fun EditProductBody(productId: String) {
    val context = LocalContext.current
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }

    val product by productViewModel.products.observeAsState()
    val loadingState by productViewModel.loading.observeAsState(initial = false)

    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isActive by remember { mutableStateOf(true) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(productId) {
        productViewModel.getProductById(productId)
    }

    LaunchedEffect(product) {
        product?.let {
            name = it.productName
            price = it.price.toString()
            description = it.description
            quantity = it.quantity.toString()
            imageUrl = it.imageUrl
            isActive = it.isActive
        }
    }

    EditProductContent(
        name = name,
        onNameChange = { name = it },
        price = price,
        onPriceChange = { price = it },
        description = description,
        onDescriptionChange = { description = it },
        quantity = quantity,
        onQuantityChange = { quantity = it },
        imageUrl = imageUrl,
        selectedImageUri = selectedImageUri,
        onImageClick = { launcher.launch("image/*") },
        isActive = isActive,
        onActiveChange = { isActive = it },
        loading = loadingState,
        onBack = { (context as? Activity)?.finish() },
        onUpdate = {
            if (name.isBlank() || price.isBlank() || quantity.isBlank()) {
                Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                return@EditProductContent
            }

            val performUpdate = { finalImageUrl: String ->
                val updatedProduct = ProductModel(
                    productId = productId,
                    productName = name,
                    price = price.toDoubleOrNull() ?: 0.0,
                    description = description,
                    quantity = quantity.toIntOrNull() ?: 0,
                    imageUrl = finalImageUrl,
                    isActive = isActive
                )
                productViewModel.updateProduct(updatedProduct) { success, message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    if (success) {
                        (context as? Activity)?.finish()
                    }
                }
            }

            if (selectedImageUri != null) {
                productViewModel.uploadImage(context, selectedImageUri!!) { success, newUrl ->
                    if (success) {
                        performUpdate(newUrl)
                    } else {
                        Toast.makeText(context, "Image upload failed: $newUrl", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                performUpdate(imageUrl)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductContent(
    name: String,
    onNameChange: (String) -> Unit,
    price: String,
    onPriceChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    imageUrl: String,
    selectedImageUri: Any?,
    onImageClick: () -> Unit,
    isActive: Boolean,
    onActiveChange: (Boolean) -> Unit,
    loading: Boolean,
    onBack: () -> Unit,
    onUpdate: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Flower", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { onImageClick() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f))
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else if (imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Current Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.Gray
                            )
                            Text("Tap to change flower image", color = Color.Gray)
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = name,
                        label = { Text("Flower Name") },
                        onValueChange = onNameChange,
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = price,
                        label = { Text("Price (Rs.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        onValueChange = onPriceChange,
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = quantity,
                        label = { Text("Stock Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = onQuantityChange,
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = description,
                        label = { Text("Description") },
                        onValueChange = onDescriptionChange,
                        minLines = 3,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isActive, onCheckedChange = onActiveChange)
                        Text("Available for Sale")
                    }
                }
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !loading,
                onClick = onUpdate
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Update Flower", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun EditProductPreview() {
    FloralTheme {
        EditProductContent(
            name = "Red Rose",
            onNameChange = {},
            price = "12.99",
            onPriceChange = {},
            description = "A beautiful red rose bouquet.",
            onDescriptionChange = {},
            quantity = "10",
            onQuantityChange = {},
            imageUrl = "",
            selectedImageUri = null,
            onImageClick = {},
            isActive = true,
            onActiveChange = {},
            loading = false,
            onBack = {},
            onUpdate = {}
        )
    }
}

