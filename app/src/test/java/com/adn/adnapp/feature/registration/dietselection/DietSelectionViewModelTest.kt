package com.adn.adnapp.feature.registration.dietselection

import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.DietRepository
import com.adn.adnapp.domain.repository.NutritionRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DietSelectionViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val diets = mockk<DietRepository>()
    private val nutrition = mockk<NutritionRepository>()
    private val auth = mockk<AuthRepository>()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { auth.getCurrentUserId() } returns "uid"
        coEvery { diets.getAvailableDiets("uid") } returns Result.success(emptyList())
    }

    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun customDiet_isPersistedAndSelected() = runTest {
        coEvery { diets.saveCustomDiet("uid", any()) } returns Result.success(Unit)
        val viewModel = DietSelectionViewModel(diets, nutrition, auth)
        advanceUntilIdle()

        viewModel.showCustomDietCreator(true)
        viewModel.onCustomNameChanged("Mi plan")
        viewModel.onCustomDescriptionChanged("Objetivos adaptados")
        viewModel.onCustomCaloriesChanged("1950")
        viewModel.onCustomProteinsChanged("140")
        viewModel.onCustomCarbsChanged("110")
        viewModel.onCustomFatsChanged("85")
        viewModel.saveCustomDiet()
        advanceUntilIdle()

        coVerify {
            diets.saveCustomDiet("uid", match {
                it.id.startsWith("custom_") && it.name == "Mi plan" &&
                    it.calories == 1950.0 && it.carbs == 110.0
            })
        }
        val state = viewModel.uiState.value
        assertEquals(state.diets.single().id, state.selectedDietId)
        assertTrue(!state.showCustomDietCreator)
    }
}
