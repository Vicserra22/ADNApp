package com.adn.adnapp.feature.dayviewer

import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.data.model.entity.Diet
import com.adn.adnapp.data.model.entity.FoodEntry
import com.adn.adnapp.data.model.entity.NutritionProfile
import com.adn.adnapp.data.model.entity.UserProfile
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.DietRepository
import com.adn.adnapp.domain.repository.FoodRepository
import com.adn.adnapp.domain.repository.NutritionRepository
import com.adn.adnapp.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DayViewerViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val foodRepository = mockk<FoodRepository>()
    private val authRepository = mockk<AuthRepository>()
    private val nutritionRepository = mockk<NutritionRepository>()
    private val dietRepository = mockk<DietRepository>()
    private val userRepository = mockk<UserRepository>()
    private val date = "2026-08-30"
    private val existing = FoodEntry(id = "food-1", name = "Arroz", calories = 200.0, carbs = 45.0)

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        every { authRepository.getCurrentUserId() } returns "uid"
        every { foodRepository.observeDailyConsumption("uid", date) } returns
            flowOf(DailyConsumption(date, 2_000.0, 120.0, 220.0, 70.0, 30.0, 1_500.0))
        every { foodRepository.observeFoodEntries("uid", date) } returns flowOf(listOf(existing))
        coEvery { userRepository.getUserProfile("uid") } returns
            Result.success(UserProfile(age = 30, weight = 75.0, height = 178.0, gender = "Hombre"))
        coEvery { nutritionRepository.getNutritionProfile("uid") } returns
            Result.success(NutritionProfile(dietId = "balanced", onboardingCompleted = true))
        coEvery { dietRepository.getDiet("balanced") } returns Result.success(
            Diet(id = "balanced", calories = 2_100.0, proteins = 120.0, carbs = 230.0,
                lipids = 70.0, sugar = 50.0, water = 2_000.0)
        )
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun openingDay_isReadOnlyAndLoadsEntriesAndGoals() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isEditing)
        assertEquals(existing, viewModel.uiState.value.entries.single())
        assertNotNull(viewModel.uiState.value.targets)
        assertNotNull(viewModel.uiState.value.score)
    }

    @Test fun manualMacros_areSavedAsSeparateEntryInsteadOfReplacingTotal() = runTest {
        val captured = slot<FoodEntry>()
        coEvery { foodRepository.saveFoodEntry("uid", capture(captured), date) } returns Result.success(Unit)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startEditing()
        viewModel.onCaloriesChanged("350")
        viewModel.onProteinsChanged("25")
        viewModel.saveManualMacros()
        advanceUntilIdle()
        assertEquals(FoodEntry.KIND_MANUAL, captured.captured.kind)
        assertEquals(350.0, captured.captured.calories, 0.0)
        assertEquals(2_000.0, viewModel.uiState.value.consumption.calories, 0.0)
        assertEquals("Entrada guardada", viewModel.uiState.value.savedMessage)
    }

    @Test fun editingAnEntry_updatesThatEntry() = runTest {
        val captured = slot<FoodEntry>()
        coEvery { foodRepository.updateFoodEntry("uid", capture(captured), date) } returns Result.success(Unit)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startEditingEntry(existing)
        viewModel.onCaloriesChanged("240")
        viewModel.saveManualMacros()
        advanceUntilIdle()
        assertEquals("food-1", captured.captured.id)
        assertEquals(240.0, captured.captured.calories, 0.0)
    }

    @Test fun deleteAndWater_useSeparateAtomicRepositoryOperations() = runTest {
        coEvery { foodRepository.deleteFoodEntry("uid", "food-1", date) } returns Result.success(Unit)
        coEvery { foodRepository.saveFoodEntry("uid", any(), date) } returns Result.success(Unit)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.deleteEntry(existing)
        viewModel.onWaterChanged("330")
        viewModel.addWater()
        advanceUntilIdle()
        coVerify { foodRepository.deleteFoodEntry("uid", "food-1", date) }
        coVerify { foodRepository.saveFoodEntry("uid", match { it.kind == FoodEntry.KIND_WATER && it.waterMl == 330.0 }, date) }
        assertTrue(viewModel.uiState.value.savedMessage != null)
    }

    private fun createViewModel() = DayViewerViewModel(
        date, foodRepository, authRepository, nutritionRepository, dietRepository, userRepository
    )
}
