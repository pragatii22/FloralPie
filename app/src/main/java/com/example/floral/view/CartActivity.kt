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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.floral.R
import com.example.floral.model.CartModel
import com.example.floral.model.OrderModel
import com.example.floral.repo.CartRepoImpl
import com.example.floral.repo.OrderRepoImpl
import com.example.floral.ui.theme.FloralTheme
import com.example.floral.viewmodel.CartViewModel
import com.example.floral.viewmodel.OrderViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class CartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FloralTheme {
                CartBody(showBack = true)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartBody(showBack: Boolean = false) {
    val context = LocalContext.current
    val cartViewModel = remember { CartViewModel(CartRepoImpl()) }
    val orderViewModel = remember { OrderViewModel(OrderRepoImpl()) }
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val cartItems by cartViewModel.cartItems.observeAsState(initial = emptyList())
    val loading by cartViewModel.loading.observeAsState(initial = false)

    LaunchedEffect(currentUser) {
        currentUser?.let {
            cartViewModel.getCartItems(it.uid)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Shopping Cart", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (showBack) {
                        IconButton(onClick = { (context as? Activity)?.finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            if (!cartItems.isNullOrEmpty()) {
                val total = cartItems?.sumOf { it.price * it.quantity } ?: 0.0
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Amount", fontSize = 14.sp, color = Color.Gray)
                            Text(
                                "$${String.format(Locale.getDefault(), "%.2f", total)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(
                            onClick = {
                                if (currentUser != null) {
                                    val order = OrderModel(
                                        userId = currentUser.uid,
                                        items = cartItems!!,
                                        totalAmount = total
                                    )
                                    orderViewModel.placeOrder(order) { success, message ->
                                        if (success) {
                                            cartViewModel.clearCart(currentUser.uid) { _, _ -> }
                                            Toast.makeText(context, "Order placed successfully!", Toast.LENGTH_LONG).show()
                                            if (showBack) (context as? Activity)?.finish()
                                        } else {
                                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.height(50.dp).width(160.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Checkout", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF8F8F8))) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (cartItems.isNullOrEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Your cart is empty",
                        fontSize = 18.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cartItems!!) { item ->
                        CartItemCard(
                            item = item,
                            onDelete = {
                                cartViewModel.removeFromCart(item.cartId) { success, message ->
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(item: CartModel, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            if (item.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.productName,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.logo),
                    error = painterResource(id = R.drawable.logo)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = item.productName,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${String.format(Locale.getDefault(), "%.2f", item.price)} x ${item.quantity}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
            
            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Remove")
            }
        }
    }
}
