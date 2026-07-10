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
                ManageOrdersBody()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageOrdersBody() {
    val context = LocalContext.current
    val orderViewModel: OrderViewModel = remember { OrderViewModel(OrderRepoImpl()) }
    val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory())
    
    val orders by orderViewModel.orders.observeAsState(initial = emptyList())
    val loading by orderViewModel.loading.observeAsState(initial = false)
    val allUsers by userViewModel.allUsers.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        orderViewModel.getAllOrders()
        userViewModel.getAllUser()
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
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(orders!!) { index, order ->
                        val customerName = allUsers?.find { it?.id == order.userId }?.name ?: "Unknown Customer"
                        OrderAdminCard(
                            order = order, 
                            index = index + 1, 
                            customerName = customerName,
                            onUpdateStatus = { newStatus ->
                                orderViewModel.updateOrderStatus(order.orderId, newStatus) { success, message ->
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
fun OrderAdminCard(
    order: OrderModel, 
    index: Int, 
    customerName: String,
    onUpdateStatus: (String) -> Unit
) {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val dateString = sdf.format(Date(order.orderDate))
    val serialNumber = String.format(Locale.getDefault(), "#%04d", index)
    
    val statusOptions = listOf("Pending", "Confirmed", "Preparing", "Out for Delivery", "Delivered", "Cancelled")
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
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
            Text(text = "Date: $dateString", fontSize = 12.sp, color = Color.Gray)
            
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
                    Text(text = "$${String.format(Locale.getDefault(), "%.2f", item.price * item.quantity)}", fontSize = 13.sp)
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
                    text = "$${String.format(Locale.getDefault(), "%.2f", order.totalAmount)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
