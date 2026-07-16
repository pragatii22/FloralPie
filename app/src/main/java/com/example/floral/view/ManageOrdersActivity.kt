package com.example.floral.view

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.floral.model.OrderModel
import com.example.floral.repo.OrderRepoImpl
import com.example.floral.ui.theme.FloralTheme
import com.example.floral.viewmodel.OrderViewModel
import com.example.floral.viewmodel.UserViewModel
import com.example.floral.viewmodel.UserViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class ManageOrdersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FloralTheme {
                val orderViewModel: OrderViewModel = remember { OrderViewModel(OrderRepoImpl()) }
                val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory())

                val orders by orderViewModel.orders.observeAsState(initial = emptyList())
                val loading by orderViewModel.loading.observeAsState(initial = false)
                val allUsers by userViewModel.allUsers.observeAsState(emptyList())

                LaunchedEffect(Unit) {
                    orderViewModel.getAllOrders()
                    userViewModel.getAllUser()
                }

                ManageOrdersContent(
                    orders = orders ?: emptyList(),
                    loading = loading,
                    allUsers = allUsers ?: emptyList(),
                    onBack = { finish() },
                    onUpdateStatus = { orderId, newStatus ->
                        orderViewModel.updateOrderStatus(orderId, newStatus) { _, message ->
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageOrdersContent(
    orders: List<OrderModel>,
    loading: Boolean,
    allUsers: List<com.example.floral.model.UserModel?>,
    onBack: () -> Unit,
    onUpdateStatus: (String, String) -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFF8F8F8),
        topBar = {
            TopAppBar(
                title = { Text("Manage Orders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (orders.isEmpty()) {
                Text(
                    "No orders found.",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 18.sp
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(orders) { index, order ->
                        val customerName = allUsers.find { it?.id == order.userId }?.name ?: "Unknown Customer"
                        OrderAdminCard(
                            order = order,
                            index = index + 1,
                            customerName = customerName,
                            onUpdateStatus = { newStatus ->
                                onUpdateStatus(order.orderId, newStatus)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderAdminCard(
    order: OrderModel, 
    index: Int, 
    customerName: String,
    onUpdateStatus: (String) -> Unit
) {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val dateString = sdf.format(Date(order.orderDate))
    val serialNumber = String.format(Locale.getDefault(), "#%04d", index)
    
    val statusOptions = listOf("Pending", "Confirmed", "Cancelled")
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order $serialNumber",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Box {
                    Surface(
                        modifier = Modifier
                            .clickable { expanded = true }
                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp)),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = order.status,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Icon(
                                Icons.Default.ArrowDropDown, 
                                contentDescription = null, 
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        statusOptions.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = {
                                    onUpdateStatus(status)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(text = "Customer: $customerName", fontWeight = FontWeight.Medium, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(text = "Flowers:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "• ${item.productName} (x${item.quantity})", fontSize = 13.sp)
                    Text(text = "Rs.${String.format(Locale.getDefault(), "%.2f", item.price * item.quantity)}", fontSize = 13.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Total Price", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    text = "Rs.${String.format(Locale.getDefault(), "%.2f", order.totalAmount)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ManageOrdersPreview() {
    FloralTheme {
        ManageOrdersContent(
            orders = listOf(
                OrderModel(
                    orderId = "1001",
                    userId = "user1",
                    items = listOf(
                        com.example.floral.model.CartModel(productName = "Red Rose", price = 12.99, quantity = 2),
                        com.example.floral.model.CartModel(productName = "White Lily", price = 15.50, quantity = 1)
                    ),
                    totalAmount = 41.48,
                    status = "Pending"
                ),
                OrderModel(
                    orderId = "1002",
                    userId = "user2",
                    items = listOf(
                        com.example.floral.model.CartModel(productName = "Sun Flower", price = 10.0, quantity = 3)
                    ),
                    totalAmount = 30.0,
                    status = "Confirmed"
                )
            ),
            loading = false,
            allUsers = listOf(
                com.example.floral.model.UserModel(id = "user1", name = "Pragati"),
                com.example.floral.model.UserModel(id = "user2", name = "Chuchu")
            ),
            onBack = {},
            onUpdateStatus = { _, _ -> }
        )
    }
}
