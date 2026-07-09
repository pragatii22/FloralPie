package com.example.floral.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
    var isActive by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        productViewModel.getProductById(productId)
    }

    LaunchedEffect(product) {
        product?.let {
            name = it.productName
            price = it.price.toString()
            description = it.description
            quantity = it.quantity.toString()
            isActive = it.isActive
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Edit Flower") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = name,
                    label = { Text("Flower Name") },
                    onValueChange = { name = it }
                )
                Spacer(modifier = Modifier.height(15.dp))
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = price,
                    label = { Text("Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    onValueChange = { price = it }
                )
                Spacer(modifier = Modifier.height(15.dp))
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = description,
                    label = { Text("Description") },
                    onValueChange = { description = it }
                )
                Spacer(modifier = Modifier.height(15.dp))
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = quantity,
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = { quantity = it }
                )
                Spacer(modifier = Modifier.height(15.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isActive, onCheckedChange = { isActive = it })
                    Text("Is Active")
                }
                Spacer(modifier = Modifier.height(25.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val updatedProduct = ProductModel(
                            productId = productId,
                            productName = name,
                            price = price.toDoubleOrNull() ?: 0.0,
                            description = description,
                            quantity = quantity.toIntOrNull() ?: 0,
                            isActive = isActive
                        )
                        productViewModel.updateProduct(updatedProduct) { success, message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            if (success) {
                                (context as? ComponentActivity)?.finish()
                            }
                        }
                    }
                ) {
                    if (loadingState) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                    else Text("Update Flower")
                }
            }
        }
    }
}
