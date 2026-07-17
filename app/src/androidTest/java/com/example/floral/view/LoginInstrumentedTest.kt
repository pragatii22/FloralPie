package com.example.floral

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.floral.view.LoginActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<LoginActivity>()

    @Test
    fun loginScreenComponentsDisplayed() {
        composeRule.onNodeWithTag("email").assertIsDisplayed()
        composeRule.onNodeWithTag("password").assertIsDisplayed()
        composeRule.onNodeWithTag("login").assertIsDisplayed()
        composeRule.onNodeWithTag("forgotPassword").assertIsDisplayed()
        composeRule.onNodeWithTag("register").assertIsDisplayed()
    }
}