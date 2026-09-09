package com.adn.adnapp.feature.recovery

import com.adn.adnapp.data.local.HealthConnectLauncher
import com.adn.adnapp.data.local.RecoveryStore
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecoveryViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = mockk<RecoveryStore>(relaxed = true)
    private val launcher = mockk<HealthConnectLauncher>(relaxed = true)
    private val data = MutableStateFlow(com.adn.adnapp.domain.model.RecoveryData())
    @Before fun setUp() { Dispatchers.setMain(dispatcher); every { store.data } returns data }
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun manualStepsKeepDateAndSource() = runTest {
        val viewModel = RecoveryViewModel(store, launcher)
        viewModel.onDateChanged("2026-09-09"); viewModel.onStepsChanged("8123"); viewModel.saveSteps(); advanceUntilIdle()
        verify { store.saveSteps(match { it.date == "2026-09-09" && it.steps == 8123 && it.source == com.adn.adnapp.domain.model.HealthSource.MANUAL }) }
        assertEquals("Pasos guardados", viewModel.uiState.value.message)
    }

    @Test fun sleepHoursBecomeMinutes() = runTest {
        val viewModel = RecoveryViewModel(store, launcher)
        viewModel.onSleepChanged("7,5"); viewModel.saveSleep()
        verify { store.saveSleep(match { it.durationMinutes == 450 }) }
    }

    @Test fun invalidSleepDoesNotWrite() = runTest {
        val viewModel = RecoveryViewModel(store, launcher)
        viewModel.onSleepChanged("30"); viewModel.saveSleep()
        assertTrue(viewModel.uiState.value.error!!.contains("sueño"))
        verify(exactly = 0) { store.saveSleep(any()) }
    }
}
