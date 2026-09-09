package com.adn.adnapp.feature.sports

import com.adn.adnapp.data.local.SportsStore
import com.adn.adnapp.domain.model.SportKind
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
class SportsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = mockk<SportsStore>(relaxed = true)
    private val data = MutableStateFlow(com.adn.adnapp.domain.model.SportsData())

    @Before fun setUp() { Dispatchers.setMain(dispatcher); every { store.data } returns data }
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun cardioSessionStoresDurationAndDistance() = runTest {
        val viewModel = SportsViewModel(store)
        viewModel.selectSport(SportKind.RUN)
        viewModel.onMinutesChanged("45")
        viewModel.onDistanceChanged("6,2")
        viewModel.addSession()
        advanceUntilIdle()

        verify { store.save(match { it.sport == SportKind.RUN && it.minutes == 45 && it.distanceKm == 6.2 }) }
        assertEquals("Sesión guardada", viewModel.uiState.value.message)
    }

    @Test fun strengthSessionKeepsSeriesRepsAndWeight() = runTest {
        val viewModel = SportsViewModel(store)
        viewModel.selectSport(SportKind.STRENGTH)
        viewModel.onMinutesChanged("30")
        viewModel.onSetsChanged("4")
        viewModel.onRepsChanged("8")
        viewModel.onWeightChanged("42.5")
        viewModel.addSession()

        verify { store.save(match { it.sets == 4 && it.reps == 8 && it.weightKg == 42.5 }) }
    }

    @Test fun zeroDurationDoesNotCreateSession() = runTest {
        val viewModel = SportsViewModel(store)
        viewModel.onMinutesChanged("0")
        viewModel.addSession()

        assertTrue(viewModel.uiState.value.error!!.contains("duración"))
        verify(exactly = 0) { store.save(any()) }
    }
}
