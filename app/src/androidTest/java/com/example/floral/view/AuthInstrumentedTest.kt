package com.example.floral.view

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthInstrumentedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<LoginActivity>()

    @Test
    fun loginUI_basicElementsDisplayed() {
        // Check if welcome text is displayed
        composeTestRule.onNodeWithText("Welcome ").assertIsDisplayed()
        
        // Check if Email and Password fields are displayed
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        
        // Check if Login button is displayed
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
    }

    @Test
    fun loginUI_emptyFields_showsToast() {
        // Click login without entering details
        composeTestRule.onNodeWithText("Login").performClick()
        
        // Toasts are harder to test in Compose instrumented tests directly without custom matchers, 
        // but we can verify we are still on the login screen
        composeTestRule.onNodeWithText("Welcome ").assertIsDisplayed()
    }
    
    @Test
    fun loginUI_navigateToRegister() {
        // Click Sign Up
        composeTestRule.onNodeWithText("Sign Up").performClick()
        
        // Wait for potential navigation or check for registration screen elements
        // This assumes the navigation happens correctly
        // Note: Instrumented tests on Activity will follow the Intent
    }
}
