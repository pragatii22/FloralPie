package com.example.floral

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.floral.view.RegistrationActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegistrationInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<RegistrationActivity>()

    @Test
    fun registrationScreenComponentsDisplayed() {
        composeRule.onNodeWithTag("fullName").assertIsDisplayed()
        composeRule.onNodeWithTag("email").assertIsDisplayed()
        composeRule.onNodeWithTag("address").assertIsDisplayed()
        composeRule.onNodeWithTag("contact").assertIsDisplayed()
        composeRule.onNodeWithTag("password").assertIsDisplayed()
        composeRule.onNodeWithTag("signUpButton").assertIsDisplayed()
    }
}