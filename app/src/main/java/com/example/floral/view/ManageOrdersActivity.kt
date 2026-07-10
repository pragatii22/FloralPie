package com.example.floral.view

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floral.model.OrderModel
import com.example.floral.repo.OrderRepoImpl
import com.example.floral.ui.theme.FloralTheme
import com.example.floral.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.*

class ManageOrdersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FloralTheme {
                ManageOrdersBody()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageOrdersBody() {
    val context = LocalContext.current
    val orderViewModel = remember { OrderViewModel(OrderRepoImpl()) }
    val orders by orderViewModel.orders.observeAsState(initial = emptyList())
    val loading by orderViewModel.loading.observeAsState(initial = false)

    LaunchedEffect(Unit) {
        orderViewModel.getAllOrders()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Orders", fontWeight = FontWeight.Bold) },
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (orders.isNullOrEmpty()) {
                Text(
                    "No orders found.",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 18.sp
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(orders!!) { order ->
                        OrderCard(order = order)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: OrderModel) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateString = sdf.format(Date(order.orderDate))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Order ID: ${order.orderId.takeLast(8)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = order.status,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Date: $dateString", fontSize = 14.sp)
            Text(text = "User ID: ${order.userId}", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "${item.productName} x ${item.quantity}", fontSize = 14.sp)
                    Text(text = "$${item.price * item.quantity}", fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Total: $${order.totalAmount}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
