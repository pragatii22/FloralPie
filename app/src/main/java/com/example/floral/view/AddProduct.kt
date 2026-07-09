package com.example.floral.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.floral.model.ProductModel
import com.example.floral.repo.ProductRepoImpl
import com.example.floral.viewmodel.ProductViewModel
import com.example.floral.ui.theme.FloralTheme

class AddProductActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FloralTheme {
                AddProductBody()
            }
        }
    }
}

@Composable
fun AddProductBody() {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(false) }

    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    var loading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(25.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = name,
                label = { Text("Product Name") },
                placeholder = { Text("Enter Product Name") },
                onValueChange = { name = it }
            )
            Spacer(modifier = Modifier.height(15.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = price,
                label = { Text("Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                placeholder = { Text("Enter Product price") },
                onValueChange = { price = it }
            )
            Spacer(modifier = Modifier.height(15.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = description,
                label = { Text("Description") },
                placeholder = { Text("Enter Product description") },
                onValueChange = { description = it }
            )
            Spacer(modifier = Modifier.height(15.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = quantity,
                label = { Text("Quantity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("Enter Product quantity") },
                onValueChange = { quantity = it }
            )
            Spacer(modifier = Modifier.height(15.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isActive,
                    onCheckedChange = { isActive = it }
                )
                Text("Is Active")
            }
            Spacer(modifier = Modifier.height(25.dp))

            ElevatedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (name.isBlank() || price.isBlank()) {
                        Toast.makeText(context, "Please fill name and price", Toast.LENGTH_SHORT).show()
                        return@ElevatedButton
                    }
                    
                    loading = true
                    val model = ProductModel(
                        productName = name,
                        price = price.toDoubleOrNull() ?: 0.0,
                        description = description,
                        quantity = quantity.toIntOrNull() ?: 0,
                        isActive = isActive
                    )
                    
                    productViewModel.addProduct(model) { success, message ->
                        loading = false
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        if (success) {
                            name = ""
                            price = ""
                            description = ""
                            quantity = ""
                            isActive = false
                        }
                    }
                }) {
                if (loading) {
                    CircularProgressIndicator()
                } else {
                    Text("Add Product")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddProductPreview() {
    FloralTheme {
        AddProductBody()
    }
}
