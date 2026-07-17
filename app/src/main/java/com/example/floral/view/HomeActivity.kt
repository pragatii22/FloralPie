package com.example.floral.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.floral.R
import com.example.floral.model.ProductModel
import com.example.floral.repo.OrderRepoImpl
import com.example.floral.repo.ProductRepoImpl
import com.example.floral.ui.theme.FloralTheme
import com.example.floral.viewmodel.OrderViewModel
import com.example.floral.viewmodel.ProductViewModel
import com.example.floral.viewmodel.UserViewModel
import com.example.floral.viewmodel.UserViewModelFactory
import androidx.compose.material.icons.filled.Inventory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FloralTheme {
                HomeBody()
            }
        }
    }
}

@Composable
fun HomeBody() {
    val context = LocalContext.current
    val productViewModel: ProductViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val orderViewModel: OrderViewModel = remember { OrderViewModel(OrderRepoImpl()) }
    val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory())

    val allProducts by productViewModel.allProducts.observeAsState(initial = emptyList())
    val orders by orderViewModel.orders.observeAsState(initial = emptyList())
    val allUsers by userViewModel.allUsers.observeAsState(emptyList())
    val loadingProducts by productViewModel.loading.observeAsState(initial = false)
    val loadingOrders by orderViewModel.loading.observeAsState(initial = false)

    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        productViewModel.getAllProduct()
        orderViewModel.getAllOrders()
        userViewModel.getAllUser()
    }

    HomeContent(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        allProducts = allProducts ?: emptyList(),
        orders = orders ?: emptyList(),
        allUsers = allUsers ?: emptyList(),
        loadingProducts = loadingProducts,
        loadingOrders = loadingOrders,
        hideTopBar = false,
        onBack = { (context as? Activity)?.finish() },
        onAddProduct = {
            context.startActivity(Intent(context, AddProductActivity::class.java))
        },
        onDeleteProduct = { flower ->
            productViewModel.deleteProduct(flower.productId) { success, message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        },
        onEditProduct = { flower ->
            val intent = Intent(context, EditProductActivity::class.java)
            intent.putExtra("productId", flower.productId)
            context.startActivity(intent)
        },
        onUpdateOrderStatus = { orderId, newStatus ->
            orderViewModel.updateOrderStatus(orderId, newStatus) { success, message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    allProducts: List<ProductModel>,
    orders: List<com.example.floral.model.OrderModel>,
    allUsers: List<com.example.floral.model.UserModel?>,
    loadingProducts: Boolean,
    loadingOrders: Boolean,
    hideTopBar: Boolean = false,
    onBack: () -> Unit,
    onAddProduct: () -> Unit,
    onDeleteProduct: (ProductModel) -> Unit,
    onEditProduct: (ProductModel) -> Unit,
    onUpdateOrderStatus: (String, String) -> Unit
) {
    var productToDelete by remember { mutableStateOf<ProductModel?>(null) }

    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Delete Flower") },
            text = { Text("Are you sure you want to delete '${productToDelete?.productName}'?") },
            confirmButton = {
                TextButton(onClick = {
                    productToDelete?.let { onDeleteProduct(it) }
                    productToDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            if (!hideTopBar) {
                TopAppBar(
                    title = { Text("Manage Flowers & Orders", fontWeight = FontWeight.Bold) },
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
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = onAddProduct,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Product")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F8F8))
        ) {

            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { onTabSelected(0) }) {
                    Text("Flowers", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { onTabSelected(1) }) {
                    Text("Orders", modifier = Modifier.padding(16.dp))
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (selectedTab == 0) {
                    if (loadingProducts) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (allProducts.isEmpty()) {
                        Text(
                            "No flowers found.",
                            modifier = Modifier.align(Alignment.Center),
                            fontSize = 18.sp
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(allProducts) { flower ->
                                FlowerCard(
                                    flower = flower,
                                    onDelete = { productToDelete = flower },
                                    onEdit = { onEditProduct(flower) }
                                )
                            }
                        }
                    }
                } else {
                    if (loadingOrders) {
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
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(orders) { index, order ->
                                val customerName =
                                    allUsers.find { it?.id == order.userId }?.name
                                        ?: "Unknown Customer"
                                OrderAdminCard(
                                    order = order,
                                    index = index + 1,
                                    customerName = customerName,
                                    onUpdateStatus = { newStatus ->
                                        onUpdateOrderStatus(order.orderId, newStatus)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun HomePreview() {
    FloralTheme {
        HomeContent(
            selectedTab = 0,
            onTabSelected = {},
            allProducts = listOf(
                ProductModel(productName = "Red Rose", price = 12.99, quantity = 10, imageUrl = ""),
                ProductModel(productName = "White Lily", price = 15.50, quantity = 5, imageUrl = "")
            ),
            orders = emptyList(),
            allUsers = emptyList(),
            loadingProducts = false,
            loadingOrders = false,
            onBack = {},
            onAddProduct = {},
            onDeleteProduct = {},
            onEditProduct = {},
            onUpdateOrderStatus = { _, _ -> }
        )
    }
}


@Composable
fun FlowerCard(
    flower: ProductModel,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (flower.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = flower.imageUrl,
                    contentDescription = flower.productName,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0F0F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Inventory, contentDescription = null, tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = flower.productName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Rs.${String.format(Locale.getDefault(), "%.2f", flower.price)}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Stock: ${flower.quantity}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}
