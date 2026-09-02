package com.adn.adnapp.feature.registration.priorities

import com.adn.adnapp.data.model.entity.UserProfile
import com.adn.adnapp.domain.model.Importance
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
class PrioritiesViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val auth = mockk<AuthRepository>()
    private val users = mockk<UserRepository>()

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { auth.getCurrentUserId() } returns "uid"
        coEvery { users.getUserProfile("uid") } returns Result.success(
            UserProfile(name = "Ana", weight = 68.0)
        )
    }

    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun save_persistsAllImportanceLevelsAndCompletesStep() = runTest {
        coEvery { users.saveUserProfile("uid", any()) } returns Result.success(Unit)
        val viewModel = PrioritiesViewModel(auth, users)
        advanceUntilIdle()
        viewModel.setTargetWeight("64")
        viewModel.setNutrition(Importance.VERY_IMPORTANT)
        viewModel.setSports(Importance.LOW)
        viewModel.setGoals(Importance.IMPORTANT)

        viewModel.save()
        advanceUntilIdle()

        coVerify {
            users.saveUserProfile("uid", match {
                it.targetWeight == 64.0 && it.nutritionImportance == Importance.VERY_IMPORTANT &&
                    it.sportsImportance == Importance.LOW && it.goalsImportance == Importance.IMPORTANT &&
                    it.prioritiesCompleted
            })
        }
    }

    @Test fun invalidTarget_doesNotWriteProfile() = runTest {
        val viewModel = PrioritiesViewModel(auth, users)
        advanceUntilIdle()
        viewModel.setTargetWeight("0")

        viewModel.save()

        assertEquals("Introduce un peso objetivo válido", viewModel.uiState.value.error)
        coVerify(exactly = 0) { users.saveUserProfile(any(), any()) }
    }
}
