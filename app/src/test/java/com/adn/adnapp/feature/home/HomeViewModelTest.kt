package com.adn.adnapp.feature.home

import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.data.model.entity.FoodEntry
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.FoodRepository
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val foods = mockk<FoodRepository>()
    private val auth = mockk<AuthRepository>()

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { auth.getCurrentUserId() } returns "uid"
        every { foods.observeDailyConsumption(eq("uid"), any()) } returns flowOf(DailyConsumption("today"))
    }

    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun firstScreenManualForm_savesAnIndependentEntry() = runTest {
        coEvery { foods.saveFoodEntry(eq("uid"), any(), any()) } returns Result.success(Unit)
        val viewModel = HomeViewModel(foods, auth)
        advanceUntilIdle()

        viewModel.onManualNameChanged("Cena manual")
        viewModel.onManualCaloriesChanged("520")
        viewModel.onManualProteinsChanged("31")
        viewModel.saveManualEntry()
        advanceUntilIdle()

        coVerify {
            foods.saveFoodEntry(
                "uid",
                match { it.kind == FoodEntry.KIND_MANUAL && it.name == "Cena manual" && it.calories == 520.0 },
                any()
            )
        }
        assertEquals("Datos añadidos al día de hoy", viewModel.uiState.value.successMessage)
    }
}
