package com.example.floral.view

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.floral.model.OrderModel
import com.example.floral.repo.CartRepoImpl
import com.example.floral.repo.OrderRepoImpl
import com.example.floral.ui.theme.FloralTheme
import com.example.floral.viewmodel.CartViewModel
import com.example.floral.viewmodel.OrderViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class CheckoutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FloralTheme {
                CheckoutBody()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutBody() {
    val context = LocalContext.current
    val cartViewModel = remember { CartViewModel(CartRepoImpl()) }
    val orderViewModel = remember { OrderViewModel(OrderRepoImpl()) }
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val cartItems by cartViewModel.cartItems.observeAsState(initial = emptyList())
    val loading by orderViewModel.loading.observeAsState(initial = false)

    LaunchedEffect(currentUser) {
        currentUser?.let {
            cartViewModel.getCartItems(it.uid)
        }
    }

    val total = cartItems?.sumOf { it.price * it.quantity } ?: 0.0

    Scaffold(
        containerColor = Color(0xFFF8F8F8),
        topBar = {
            TopAppBar(
                title = { Text("Checkout", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        if (currentUser != null && !cartItems.isNullOrEmpty()) {
                            val order = OrderModel(
                                userId = currentUser.uid,
                                items = cartItems!!,
                                totalAmount = total,
                                status = "Pending"
                            )
                            orderViewModel.placeOrder(order) { success, message ->
                                if (success) {
                                    cartViewModel.clearCart(currentUser.uid) { _, _ -> }
                                    Toast.makeText(context, "Order placed successfully!", Toast.LENGTH_LONG).show()
                                    (context as? Activity)?.finish()
                                } else {
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !cartItems.isNullOrEmpty() && !loading
                ) {
                    if (loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Place Order", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Selected Flowers", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            items(cartItems ?: emptyList()) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${item.productName} x ${item.quantity}", fontSize = 16.sp)
                    Text("$${String.format(Locale.getDefault(), "%.2f", item.price * item.quantity)}", fontWeight = FontWeight.SemiBold)
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Price", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(
                        "$${String.format(Locale.getDefault(), "%.2f", total)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                Text("Payment Method", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = true, onClick = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cash On Delivery", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
