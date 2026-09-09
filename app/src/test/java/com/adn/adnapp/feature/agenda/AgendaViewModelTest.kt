package com.adn.adnapp.feature.agenda

import com.adn.adnapp.data.local.AgendaReminderScheduler
import com.adn.adnapp.data.local.AgendaStore
import com.adn.adnapp.domain.model.AgendaItem
import com.adn.adnapp.domain.model.AgendaItemKind
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
class AgendaViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = mockk<AgendaStore>(relaxed = true)
    private val scheduler = mockk<AgendaReminderScheduler>(relaxed = true)
    private val data = MutableStateFlow(com.adn.adnapp.domain.model.AgendaData())

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { store.data } returns data
    }

    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun blankCaptureExplainsWhatIsMissing() = runTest {
        val viewModel = AgendaViewModel(store, scheduler) { LocalDate.of(2026, 9, 9) }
        viewModel.addItem()

        assertEquals("Escribe una acción para añadirla", viewModel.uiState.value.error)
        verify(exactly = 0) { store.saveItem(any()) }
    }

    @Test fun inboxCaptureStoresAnUndatedItemAndSchedulesIt() = runTest {
        val viewModel = AgendaViewModel(store, scheduler) { LocalDate.of(2026, 9, 9) }
        viewModel.setView(AgendaView.INBOX)
        viewModel.onTitleChanged("Comprar ingredientes")
        viewModel.addItem()
        advanceUntilIdle()

        verify { store.saveItem(match { it.title == "Comprar ingredientes" && it.date == null }) }
        verify { scheduler.schedule(match { it.title == "Comprar ingredientes" }) }
        assertTrue(viewModel.uiState.value.message!!.contains("agenda"))
    }

    @Test fun reminderRejectsAnInvalidTimeBeforeWriting() = runTest {
        val viewModel = AgendaViewModel(store, scheduler) { LocalDate.of(2026, 9, 9) }
        viewModel.setKind(AgendaItemKind.REMINDER)
        viewModel.onTitleChanged("Tomar medicación")
        viewModel.onTimeChanged("9:00")
        viewModel.addItem()

        assertEquals("La hora debe tener formato HH:mm", viewModel.uiState.value.error)
        verify(exactly = 0) { store.saveItem(any()) }
    }

    @Test fun projectCapturePersistsGoalAndClearsForm() = runTest {
        val viewModel = AgendaViewModel(store, scheduler) { LocalDate.of(2026, 9, 9) }
        viewModel.onProjectNameChanged("Preparar carrera")
        viewModel.onProjectGoalChanged("Correr 10 km")
        viewModel.addProject()

        verify { store.saveProject(match { it.name == "Preparar carrera" && it.goal == "Correr 10 km" }) }
        assertEquals("", viewModel.uiState.value.projectNameInput)
    }
}
