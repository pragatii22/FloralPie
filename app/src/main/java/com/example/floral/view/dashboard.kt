package com.example.floral.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import com.example.floral.R
import com.example.floral.repo.ProductRepoImpl
import com.example.floral.ui.theme.FloralTheme
import com.example.floral.viewmodel.ProductViewModel

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FloralTheme {
                DashboardBody()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardBody() {
    val context = LocalContext.current
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val allProducts by productViewModel.allProducts.observeAsState(initial = emptyList())
    val loading by productViewModel.loading.observeAsState(initial = false)

    LaunchedEffect(Unit) {
        productViewModel.getAllProduct()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Floral Bloom & Brew", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                        auth.signOut()
                        context.startActivity(Intent(context, LoginActivity::class.java))
                        (context as? ComponentActivity)?.finish()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Background Image
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.3f
            )

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (allProducts.isNullOrEmpty()) {
                Text(
                    "No flowers found.",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 18.sp
                )
            } else {
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allProducts?.size ?: 0) { index ->
                        val flower = allProducts!![index]
                        UserFlowerCard(flower = flower)
                    }
                }
            }
        }
    }
}

@Composable
fun UserFlowerCard(flower: com.example.floral.model.ProductModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Image(
                painter = painterResource(id = R.drawable.flower1),
                contentDescription = flower.productName,
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = flower.productName, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(text = "$${flower.price}", color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { /* Add to cart logic */ },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Add to Cart", fontSize = 12.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardActivityPreview() {
    FloralTheme {
        DashboardBody()
    }
}
