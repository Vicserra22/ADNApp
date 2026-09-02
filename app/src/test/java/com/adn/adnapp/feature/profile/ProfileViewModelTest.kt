package com.adn.adnapp.feature.profile

import com.adn.adnapp.data.model.entity.Diet
import com.adn.adnapp.data.model.entity.NutritionProfile
import com.adn.adnapp.data.model.entity.UserProfile
import com.adn.adnapp.domain.model.BodyGoal
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.DietRepository
import com.adn.adnapp.domain.repository.NutritionRepository
import com.adn.adnapp.domain.repository.UserRepository
import com.adn.adnapp.domain.repository.WeightRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val auth = mockk<AuthRepository>()
    private val users = mockk<UserRepository>()
    private val weights = mockk<WeightRepository>()
    private val nutrition = mockk<NutritionRepository>()
    private val diets = mockk<DietRepository>()
    private val profile = UserProfile(
        name = "Ana", age = 31, weight = 68.0, height = 170.0, gender = "Mujer",
        targetWeight = 64.0, prioritiesCompleted = true
    )

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { auth.getCurrentUserId() } returns "uid"
        coEvery { users.getUserProfile("uid") } returns Result.success(profile)
        coEvery { diets.getAvailableDiets() } returns Result.success(listOf(Diet(id = "balanced", name = "Equilibrada")))
        coEvery { nutrition.getNutritionProfile("uid") } returns Result.success(NutritionProfile("balanced", true))
        every { weights.observeWeightHistory("uid") } returns flowOf(emptyList())
    }

    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun saveProfile_persistsEditedGoalAndDiet() = runTest {
        coEvery { users.saveUserProfile("uid", any()) } returns Result.success(Unit)
        coEvery { nutrition.updateDiet("uid", "balanced") } returns Result.success(Unit)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.startEditing()
        viewModel.onBodyGoalChanged(BodyGoal.LOSE_WEIGHT)
        viewModel.saveProfile()
        advanceUntilIdle()

        coVerify { users.saveUserProfile("uid", match { it.bodyGoal == BodyGoal.LOSE_WEIGHT }) }
        coVerify { nutrition.updateDiet("uid", "balanced") }
        assertFalse(viewModel.uiState.value.isEditing)
        assertEquals("Perfil actualizado", viewModel.uiState.value.message)
    }

    private fun createViewModel() = ProfileViewModel(auth, users, weights, nutrition, diets)
}
