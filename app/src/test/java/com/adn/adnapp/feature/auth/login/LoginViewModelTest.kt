package com.adn.adnapp.feature.auth.login

import app.cash.turbine.test
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)
    
    private lateinit var viewModel: LoginViewModel
    
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(authRepository, userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onEmailChanged_updatesUiState() = runTest {
        val testEmail = "test@example.com"
        
        viewModel.onEmailChanged(testEmail)
        
        assertEquals(testEmail, viewModel.uiState.value.email)
    }

    @Test
    fun onLoginClicked_emptyFields_showsError() = runTest {
        viewModel.onEmailChanged("")
        
        viewModel.onLoginClicked("")
        
        assertEquals("Completa todos los campos", viewModel.uiState.value.error)
    }

    @Test
    fun onLoginClicked_success_emitsNavigateToMain() = runTest {
        val testEmail = "test@example.com"
        val testPassword = "password"
        val testUid = "user_123"
        
        coEvery { authRepository.login(testEmail, testPassword) } returns Result.success(Unit)
        coEvery { authRepository.getCurrentUserId() } returns testUid
        coEvery { userRepository.userExists(testUid) } returns Result.success(true)

        viewModel.onEmailChanged(testEmail)
        
        viewModel.eventFlow.test {
            viewModel.onLoginClicked(testPassword)
            
            val event = awaitItem()
            assertEquals(LoginEvent.NavigateToMain, event)
        }
    }
}
