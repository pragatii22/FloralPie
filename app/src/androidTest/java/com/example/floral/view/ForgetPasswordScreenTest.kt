package com.example.floral

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.floral.view.ForgetPasswordActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ForgetPasswordInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ForgetPasswordActivity>()

    @Test
    fun forgotPasswordScreenComponentsDisplayed() {
        composeRule.onNodeWithTag("email").assertIsDisplayed()
        composeRule.onNodeWithTag("sendResetLink").assertIsDisplayed()
    }
}