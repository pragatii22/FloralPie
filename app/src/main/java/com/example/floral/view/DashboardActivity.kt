package com.example.floral.view

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.floral.R
import com.example.floral.model.CartModel
import com.example.floral.model.ProductModel
import com.example.floral.model.UserModel
import com.example.floral.repo.CartRepoImpl
import com.example.floral.repo.ProductRepoImpl
import com.example.floral.repo.UserRepoImpl
import com.example.floral.ui.theme.FloralTheme
import com.example.floral.viewmodel.CartViewModel
import com.example.floral.viewmodel.ProductViewModel
import com.example.floral.viewmodel.UserViewModel
import com.example.floral.viewmodel.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Locale

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FloralTheme {
                DashboardMainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardMainScreen() {
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory())
    val userState by userViewModel.users.observeAsState()
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val cartViewModel = remember { CartViewModel(CartRepoImpl()) }
    val allProducts by productViewModel.allProducts.observeAsState(initial = emptyList())
    val productsLoading by productViewModel.loading.observeAsState(initial = false)

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            userViewModel.getUserId(uid)
        }
    }

    LaunchedEffect(Unit) {
        productViewModel.getAllProduct()
    }

    DashboardContent(
        userState = userState,
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        drawerState = drawerState,
        onMenuClick = { scope.launch { drawerState.open() } },
        onCloseDrawer = { scope.launch { drawerState.close() } },
        allProducts = allProducts ?: emptyList(),
        productsLoading = productsLoading,
        onAddToCart = { flower ->
            if (currentUser != null) {
                val cartItem = CartModel(
                    productId = flower.productId,
                    userId = currentUser.uid,
                    productName = flower.productName,
                    price = flower.price,
                    quantity = 1,
                    imageUrl = flower.imageUrl
                )
                cartViewModel.addToCart(cartItem) { _, message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Please login to add to cart", Toast.LENGTH_SHORT).show()
            }
        },
        onViewProductDetails = { productId ->
            val intent = Intent(context, ProductDetailsActivity::class.java)
            intent.putExtra("productId", productId)
            context.startActivity(intent)
        },
        onNavigateToActivity = { activityClass ->
            context.startActivity(Intent(context, activityClass))
        },
        onLogout = {
            auth.signOut()
            context.startActivity(Intent(context, LoginActivity::class.java))
            (context as? ComponentActivity)?.finish()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    userState: UserModel?,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    drawerState: DrawerState,
    onMenuClick: () -> Unit,
    onCloseDrawer: () -> Unit,
    allProducts: List<ProductModel>,
    productsLoading: Boolean,
    onAddToCart: (ProductModel) -> Unit,
    onViewProductDetails: (String) -> Unit,
    onNavigateToActivity: (Class<*>) -> Unit,
    onLogout: () -> Unit
) {
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
                                    model = userState.imageUrl,
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
                            text = userState?.name ?: "User Name",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = userState?.email ?: "user@example.com",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                NavigationDrawerItem(
                    label = { Text("Home Catalog") },
                    selected = selectedTab == 0,
                    onClick = {
                        onTabSelected(0)
                        onCloseDrawer()
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("My Cart") },
                    selected = selectedTab == 1,
                    onClick = {
                        onTabSelected(1)
                        onCloseDrawer()
                    },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("My Profile") },
                    selected = selectedTab == 2,
                    onClick = {
                        onTabSelected(2)
                        onCloseDrawer()
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        onCloseDrawer()
                        onLogout()
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    modifier = Modifier.padding(12.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedIconColor = MaterialTheme.colorScheme.error, unselectedTextColor = MaterialTheme.colorScheme.error)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                val title = when(selectedTab) {
                    0 -> "Floral Bloom"
                    1 -> "My Cart"
                    2 -> "My Profile"
                    else -> "Floral Bloom"
                }
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                            if (selectedTab == 0) {
                                Text("Fresh Flowers for You", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        if (selectedTab == 0) {
                            IconButton(onClick = { onTabSelected(1) }) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                            }
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
                        onClick = { onTabSelected(0) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { onTabSelected(1) },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") },
                        label = { Text("Cart") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { onTabSelected(2) },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") }
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (selectedTab) {
                    0 -> DashboardHomeBody(
                        allProducts = allProducts,
                        loading = productsLoading,
                        onAddToCart = onAddToCart,
                        onViewDetails = onViewProductDetails
                    )
                    1 -> CartBody(showBack = false, hideTopBar = true)
                    2 -> ProfileBody(onBack = { onTabSelected(0) }, hideTopBar = true)
                }
            }
        }
    }
}

@Composable
fun DashboardHomeBody(
    allProducts: List<ProductModel>,
    loading: Boolean,
    onAddToCart: (ProductModel) -> Unit,
    onViewDetails: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F8F8))) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (allProducts.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text("No flowers available at the moment.", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(allProducts) { flower ->
                    UserFlowerCard(
                        flower = flower,
                        onAddToCart = { onAddToCart(flower) },
                        onViewDetails = { onViewDetails(flower.productId) }
                    )
                }
            }
        }
    }
}

@Composable
fun UserFlowerCard(flower: ProductModel, onAddToCart: () -> Unit, onViewDetails: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onViewDetails() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(modifier = Modifier.height(160.dp).fillMaxWidth()) {
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
                
                // Price Tag Overlay
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Rs. ${String.format(Locale.getDefault(), "%.2f", flower.price)}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = flower.productName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Qty: ${flower.quantity}",
                    fontSize = 13.sp,
                    color = if (flower.quantity > 0) Color(0xFF4CAF50) else Color.Red,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onViewDetails,
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 1.dp)
                    ) {
                        Text("Details", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = onAddToCart,
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(8.dp),
                        enabled = flower.quantity > 0
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    FloralTheme {
        DashboardContent(
            userState = UserModel(
                name = "Pragati gaire",
                email = "pragati@example.com",
                role = "user"
            ),
            selectedTab = 0,
            onTabSelected = {},
            drawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
            onMenuClick = {},
            onCloseDrawer = {},
            allProducts = listOf(
                ProductModel(
                    productId = "1",
                    productName = "Red Rose",
                    price = 12.99,
                    quantity = 10,
                    imageUrl = ""
                ),
                ProductModel(
                    productId = "2",
                    productName = "White Lily",
                    price = 15.50,
                    quantity = 5,
                    imageUrl = ""
                )
            ),
            productsLoading = false,
            onAddToCart = {},
            onViewProductDetails = {},
            onNavigateToActivity = {},
            onLogout = {}
        )
    }
}


