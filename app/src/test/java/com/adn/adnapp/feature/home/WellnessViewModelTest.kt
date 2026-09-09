package com.adn.adnapp.feature.home

import com.adn.adnapp.data.local.WellnessData
import com.adn.adnapp.data.local.WellnessStore
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.FoodRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WellnessViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = mockk<WellnessStore>(relaxed = true)
    private val auth = mockk<AuthRepository>()
    private val foods = mockk<FoodRepository>(relaxed = true)
    private val data = MutableStateFlow(WellnessData())

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { store.data } returns data
        every { auth.getCurrentUserId() } returns null
    }

    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun unauthenticatedWaterIsStoredLocallyAndCanBeUndone() = runTest {
        val viewModel = WellnessViewModel(store, auth, foods) { LocalDate.of(2026, 9, 9) }
        viewModel.addWater(330.0)
        advanceUntilIdle()

        verify { store.addWater(match { it.amountMl == 330.0 && it.date == "2026-09-09" }) }
        assertTrue(viewModel.uiState.value.message!!.contains("dispositivo"))
    }

    @Test fun syncedWaterStoresRemoteIdentityForUndo() = runTest {
        every { auth.getCurrentUserId() } returns "user"
        coEvery { foods.saveWaterEntry("user", "2026-09-09", 250.0) } returns Result.success("entry-1")
        val viewModel = WellnessViewModel(store, auth, foods) { LocalDate.of(2026, 9, 9) }
        viewModel.addQuickWater()
        advanceUntilIdle()

        verify { store.addWater(match { it.amountMl == 250.0 }) }
        verify { store.attachRemoteEntry(any(), "entry-1") }
        coVerify(exactly = 1) { foods.saveWaterEntry("user", "2026-09-09", 250.0) }
    }

    @Test fun invalidSunAmountDoesNotWrite() = runTest {
        val viewModel = WellnessViewModel(store, auth, foods) { LocalDate.of(2026, 9, 9) }
        viewModel.onSunInputChanged("0")
        viewModel.addSunFromInput()

        verify(exactly = 0) { store.addSun(any()) }
        assertEquals("Introduce entre 1 y 720 minutos", viewModel.uiState.value.error)
    }
}
