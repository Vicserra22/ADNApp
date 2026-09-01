package com.adn.adnapp.feature.dayviewer

import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.FoodRepository
import io.mockk.coEvery
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DayViewerViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val foodRepository = mockk<FoodRepository>()
    private val authRepository = mockk<AuthRepository>()
    private val date = "2026-08-30"

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        every { authRepository.getCurrentUserId() } returns "uid"
        every { foodRepository.observeDailyConsumption("uid", date) } returns
            flowOf(DailyConsumption(date, 2000.0, 120.0, 220.0, 70.0))
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun openingDay_startsInReadOnlyMode() = runTest {
        val viewModel = DayViewerViewModel(date, foodRepository, authRepository)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isEditing)
        assertEquals(2000.0, viewModel.uiState.value.consumption.calories, 0.0)
    }

    @Test fun explicitEdit_canSaveManualMacrosForSelectedDate() = runTest {
        coEvery { foodRepository.setDailyConsumption("uid", any()) } returns Result.success(Unit)
        val viewModel = DayViewerViewModel(date, foodRepository, authRepository)
        advanceUntilIdle()
        viewModel.startEditing()
        assertTrue(viewModel.uiState.value.isEditing)
        viewModel.onCaloriesChanged("2100")
        viewModel.onProteinsChanged("130")
        viewModel.onCarbsChanged("230")
        viewModel.onFatsChanged("75")
        viewModel.saveManualMacros()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isEditing)
        assertEquals(2100.0, viewModel.uiState.value.consumption.calories, 0.0)
        assertEquals("Macros guardados", viewModel.uiState.value.savedMessage)
    }
}
