package com.example.floral.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.floral.model.UserModel
import com.example.floral.repo.OrderRepoImpl
import com.example.floral.repo.ProductRepoImpl
import com.example.floral.ui.theme.FloralTheme
import com.example.floral.viewmodel.OrderViewModel
import com.example.floral.viewmodel.ProductViewModel
import com.example.floral.viewmodel.UserViewModel
import com.example.floral.viewmodel.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AdminDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FloralTheme {
                AdminDashboardMainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardMainScreen() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    
    val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory())
    val userState by userViewModel.users.observeAsState()
    
    val productViewModel: ProductViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val orderViewModel: OrderViewModel = remember { OrderViewModel(OrderRepoImpl()) }
    
    val allProducts by productViewModel.allProducts.observeAsState(initial = emptyList())
    val orders by orderViewModel.orders.observeAsState(initial = emptyList())
    val allUsers by userViewModel.allUsers.observeAsState(emptyList())
    
    val loadingProducts by productViewModel.loading.observeAsState(initial = false)
    val loadingOrders by orderViewModel.loading.observeAsState(initial = false)

    var selectedTab by remember { mutableIntStateOf(0) }
    var flowerOrderTab by remember { mutableIntStateOf(0) }
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            userViewModel.getUserId(uid)
        }
    }

    LaunchedEffect(Unit) {
        productViewModel.getAllProduct()
        orderViewModel.getAllOrders()
        userViewModel.getAllUser()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = Color.White
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(24.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        ) {
                            if (userState?.imageUrl?.isNotEmpty() == true) {
                                AsyncImage(
                                    model = userState?.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().padding(12.dp),
                                    tint = Color.Gray
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = userState?.name ?: "Admin Name",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = userState?.email ?: "admin@example.com",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                NavigationDrawerItem(
                    label = { Text("Dashboard Home") },
                    selected = selectedTab == 0,
                    onClick = { 
                        selectedTab = 0
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("User Management") },
                    selected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 1
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.People, contentDescription = null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        auth.signOut()
                        context.startActivity(Intent(context, LoginActivity::class.java))
                        (context as? ComponentActivity)?.finish()
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    modifier = Modifier.padding(12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = MaterialTheme.colorScheme.error, 
                        unselectedTextColor = MaterialTheme.colorScheme.error
                    )
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                val title = when(selectedTab) {
                    0 -> "Manage Flowers & Orders"
                    1 -> "Manage Users"
                    2 -> "Admin Profile"
                    else -> "Admin Dashboard"
                }
                CenterAlignedTopAppBar(
                    title = {
                        Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Inventory, contentDescription = "Flowers") },
                        label = { Text("Flowers") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.People, contentDescription = "Users") },
                        label = { Text("Users") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") }
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (selectedTab) {
                    0 -> HomeContent(
                        selectedTab = flowerOrderTab,
                        onTabSelected = { flowerOrderTab = it },
                        allProducts = allProducts ?: emptyList(),
                        orders = orders ?: emptyList(),
                        allUsers = allUsers ?: emptyList(),
                        loadingProducts = loadingProducts,
                        loadingOrders = loadingOrders,
                        hideTopBar = true,
                        onBack = { selectedTab = 0 },
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
                            orderViewModel.updateOrderStatus(orderId, newStatus) { _, message ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    1 -> ManageUsersContent(
                        allUsers = allUsers ?: emptyList(),
                        onBack = { selectedTab = 0 },
                        onDeleteClick = { user ->
                            userViewModel.deleteUsr(user.id) { success, message ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                if (success) userViewModel.getAllUser()
                            }
                        },
                        hideTopBar = true
                    )
                    2 -> ProfileBody(
                        onBack = { selectedTab = 0 },
                        hideTopBar = true
                    )
                }
            }
        }
    }
}
