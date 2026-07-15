package com.example.floral.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.floral.R
import com.example.floral.ui.theme.FloralTheme
import kotlinx.coroutines.delay
import com.google.firebase.auth.FirebaseAuth
import com.example.floral.repo.UserRepoImpl

class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SplashBody()
        }
    }
}

@Composable
fun SplashBody() {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        delay(3000)
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Reload user to ensure the session is still valid and account not disabled
            currentUser.reload().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val repo = UserRepoImpl()
                    repo.getUserId(currentUser.uid) { success, _, user ->
                        if (success && user != null) {
                            val intent = if (user.role == "admin") {
                                Intent(context, AdminDashboardActivity::class.java)
                            } else {
                                Intent(context, DashboardActivity::class.java)
                            }
                            context.startActivity(intent)
                        } else {
                            // Fallback to login if user data cannot be fetched
                            context.startActivity(Intent(context, LoginActivity::class.java))
                        }
                        (context as? ComponentActivity)?.finish()
                    }
                } else {
                    // Session might be expired or malformed, sign out and go to login
                    auth.signOut()
                    context.startActivity(Intent(context, LoginActivity::class.java))
                    (context as? ComponentActivity)?.finish()
                }
            }
        } else {
            // No user logged in
            context.startActivity(Intent(context, LoginActivity::class.java))
            (context as? ComponentActivity)?.finish()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .height(200.dp)
                .width(200.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        CircularProgressIndicator(color = Color.Blue)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview3() {
    FloralTheme {
        SplashBody()
    }
}
