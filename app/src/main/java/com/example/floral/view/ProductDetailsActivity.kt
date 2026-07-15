package com.example.floral.view

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.floral.R
import com.example.floral.model.CartModel
import com.example.floral.repo.CartRepoImpl
import com.example.floral.repo.ProductRepoImpl
import com.example.floral.ui.theme.FloralTheme
import com.example.floral.viewmodel.CartViewModel
import com.example.floral.viewmodel.ProductViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class ProductDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val productId = intent.getStringExtra("productId") ?: ""
        setContent {
            FloralTheme {
                ProductDetailsBody(productId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsBody(productId: String) {
    val context = LocalContext.current
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val cartViewModel = remember { CartViewModel(CartRepoImpl()) }
    val product by productViewModel.products.observeAsState()
    val loading by productViewModel.loading.observeAsState(initial = true)
    
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val isAdmin = currentUser?.email == "admin@floral.com"

    LaunchedEffect(productId) {
        if (productId.isNotEmpty()) {
            productViewModel.getProductById(productId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flower Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
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
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF8F8F8))) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (product == null) {
                Text(
                    "Product not found.",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 18.sp
                )
            } else {
                val flower = product!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Large Image
                    Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                        if (flower.imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = flower.imageUrl,
                                contentDescription = flower.productName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.logo),
                                error = painterResource(id = R.drawable.logo)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = flower.productName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = flower.productName,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "$${String.format(Locale.getDefault(), "%.2f", flower.price)}",
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Surface(
                            color = if (flower.quantity > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (flower.quantity > 0) "In Stock: ${flower.quantity}" else "Out of Stock",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 14.sp,
                                color = if (flower.quantity > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Product Description",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = flower.description,
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        if (!isAdmin) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        if (currentUser != null) {
                                            val cartItem = CartModel(
                                                productId = flower.productId,
                                                userId = currentUser.uid,
                                                productName = flower.productName,
                                                price = flower.price,
                                                quantity = 1,
                                                imageUrl = flower.imageUrl
                                            )
                                            cartViewModel.addToCart(cartItem) { success, message ->
                                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    enabled = flower.quantity > 0
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add to Cart")
                                }

                                Button(
                                    onClick = {
                                        // Simple "Buy Now" could just be adding to cart and navigating to cart
                                        if (currentUser != null) {
                                            val cartItem = CartModel(
                                                productId = flower.productId,
                                                userId = currentUser.uid,
                                                productName = flower.productName,
                                                price = flower.price,
                                                quantity = 1,
                                                imageUrl = flower.imageUrl
                                            )
                                            cartViewModel.addToCart(cartItem) { success, _ ->
                                                if (success) {
                                                    // Navigate to CartActivity or Checkout
                                                    // For now, let's just go to Cart
                                                    context.startActivity(android.content.Intent(context, CartActivity::class.java))
                                                }
                                            }
                                        } else {
                                            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    enabled = flower.quantity > 0
                                ) {
                                    Text("Buy Now")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
