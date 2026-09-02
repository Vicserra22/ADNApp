package com.adn.adnapp.feature.splash

import com.adn.adnapp.data.model.entity.NutritionProfile
import com.adn.adnapp.data.model.entity.UserProfile
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.NutritionRepository
import com.adn.adnapp.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val auth = mockk<AuthRepository>()
    private val users = mockk<UserRepository>()
    private val nutrition = mockk<NutritionRepository>()

    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun unauthenticatedUser_goesToWelcome() = runTest {
        every { auth.isAuthenticated() } returns false
        val viewModel = SplashViewModel(auth, users, nutrition)
        viewModel.onAppStarted()
        advanceUntilIdle()
        assertEquals(SplashDestination.WELCOME, viewModel.uiState.value.destination)
    }

    @Test fun missingGeneralProfile_goesToUserInfo() = runTest {
        every { auth.isAuthenticated() } returns true
        every { auth.getCurrentUserId() } returns "uid"
        coEvery { users.userExists("uid") } returns Result.success(false)
        val viewModel = SplashViewModel(auth, users, nutrition)
        viewModel.onAppStarted()
        advanceUntilIdle()
        assertEquals(SplashDestination.USER_INFO, viewModel.uiState.value.destination)
    }

    @Test fun completedNutritionOnboarding_goesToMain() = runTest {
        every { auth.isAuthenticated() } returns true
        every { auth.getCurrentUserId() } returns "uid"
        coEvery { users.userExists("uid") } returns Result.success(true)
        coEvery { users.getUserProfile("uid") } returns Result.success(UserProfile(prioritiesCompleted = true))
        coEvery { nutrition.getNutritionProfile("uid") } returns
            Result.success(NutritionProfile("balanced", true))
        val viewModel = SplashViewModel(auth, users, nutrition)
        viewModel.onAppStarted()
        advanceUntilIdle()
        assertEquals(SplashDestination.MAIN, viewModel.uiState.value.destination)
    }
}
