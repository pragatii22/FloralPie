package com.example.floral.view

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3Api::class)
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
    var isActive by remember { mutableStateOf(true) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Flower", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = name,
                        label = { Text("Flower Name") },
                        onValueChange = { name = it },
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = price,
                        label = { Text("Price ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        onValueChange = { price = it },
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = quantity,
                        label = { Text("Stock Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = { quantity = it },
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = description,
                        label = { Text("Description") },
                        onValueChange = { description = it },
                        minLines = 3,
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = imageUrl,
                        label = { Text("Image URL") },
                        onValueChange = { imageUrl = it },
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isActive, onCheckedChange = { isActive = it })
                        Text("Available for Sale")
                    }
                }
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                onClick = {
                    if (name.isBlank() || price.isBlank() || quantity.isBlank()) {
                        Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    val updatedProduct = ProductModel(
                        productId = productId,
                        productName = name,
                        price = price.toDoubleOrNull() ?: 0.0,
                        description = description,
                        quantity = quantity.toIntOrNull() ?: 0,
                        imageUrl = imageUrl,
                        isActive = isActive
                    )
                    productViewModel.updateProduct(updatedProduct) { success, message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        if (success) {
                            (context as? Activity)?.finish()
                        }
                    }
                }
            ) {
                if (loadingState) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Update Flower", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
