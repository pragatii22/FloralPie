package com.example.floral.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.floral.repo.UserRepo
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class UserViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var repo: UserRepo

    private lateinit var viewModel: UserViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = UserViewModel(repo)
    }

    @Test
    fun `login success sets loading to false and calls callback`() {
        val email = "test@example.com"
        val password = "password"
        
        // Mocking repo.login to simulate success
        doAnswer {
            val callback = it.getArgument<(Boolean, String) -> Unit>(2)
            callback(true, "Login Successful")
            null
        }.`when`(repo).login(eq(email), eq(password), any())

        var resultSuccess = false
        viewModel.login(email, password) { success, _ ->
            resultSuccess = success
        }

        assertTrue(resultSuccess)
        assertEquals(false, viewModel.loading.value)
    }

    @Test
    fun `login failure sets loading to false and calls callback`() {
        val email = "test@example.com"
        val password = "wrong"
        
        doAnswer {
            val callback = it.getArgument<(Boolean, String) -> Unit>(2)
            callback(false, "Login Failed")
            null
        }.`when`(repo).login(eq(email), eq(password), any())

        var resultSuccess = true
        viewModel.login(email, password) { success, _ ->
            resultSuccess = success
        }

        assertFalse(resultSuccess)
        assertEquals(false, viewModel.loading.value)
    }

    @Test
    fun `register success calls callback`() {
        val email = "test@example.com"
        val password = "password"
        
        doAnswer {
            val callback = it.getArgument<(Boolean, String, String) -> Unit>(2)
            callback(true, "Success", "uid123")
            null
        }.`when`(repo).register(eq(email), eq(password), any())

        var resultSuccess = false
        viewModel.register(email, password) { success, _, _ ->
            resultSuccess = success
        }

        assertTrue(resultSuccess)
    }
}
