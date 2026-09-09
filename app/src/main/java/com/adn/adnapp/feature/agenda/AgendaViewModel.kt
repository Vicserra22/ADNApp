package com.adn.adnapp.feature.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adn.adnapp.data.local.AgendaReminderScheduler
import com.adn.adnapp.data.local.AgendaStore
import com.adn.adnapp.domain.model.AgendaItem
import com.adn.adnapp.domain.model.AgendaItemKind
import com.adn.adnapp.domain.model.AgendaProject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AgendaView(val label: String) { TODAY("Hoy"), WEEK("Semana"), INBOX("Bandeja"), PROJECTS("Proyectos") }

data class AgendaUiState(
    val selectedDate: String = LocalDate.now().toString(),
    val view: AgendaView = AgendaView.TODAY,
    val items: List<AgendaItem> = emptyList(),
    val inboxCount: Int = 0,
    val projects: List<AgendaProject> = emptyList(),
    val titleInput: String = "",
    val noteInput: String = "",
    val timeInput: String = "",
    val kind: AgendaItemKind = AgendaItemKind.TASK,
    val projectId: String? = null,
    val projectNameInput: String = "",
    val projectGoalInput: String = "",
    val error: String? = null,
    val message: String? = null
)

class AgendaViewModel(
    private val store: AgendaStore,
    private val scheduler: AgendaReminderScheduler,
    private val today: () -> LocalDate = { LocalDate.now() }
) : ViewModel() {
    private val _uiState = MutableStateFlow(AgendaUiState(selectedDate = today().toString()))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            store.data.collect { data ->
                _uiState.update { state ->
                    val selected = LocalDate.parse(state.selectedDate)
                    val weekEnd = selected.plusDays(6)
                    val visible = when (state.view) {
                        AgendaView.TODAY -> data.items.filter { it.date == state.selectedDate }
                        AgendaView.WEEK -> data.items.filter { item ->
                            item.date?.let { date -> val parsed = LocalDate.parse(date); parsed in selected..weekEnd } == true
                        }
                        AgendaView.INBOX -> data.items.filter { it.date == null }
                        AgendaView.PROJECTS -> data.items.filter { it.projectId != null }
                    }.sortedWith(compareBy<AgendaItem> { it.completed }.thenBy { it.date ?: "" }.thenBy { it.time })
                    state.copy(items = visible, inboxCount = data.items.count { it.date == null }, projects = data.projects)
                }
            }
        }
    }

    fun setView(view: AgendaView) = _uiState.update { it.copy(view = view, error = null) }
    fun previousDay() = moveDay(-1)
    fun nextDay() = moveDay(1)
    fun chooseDate(date: String) = _uiState.update { it.copy(selectedDate = date, view = AgendaView.TODAY) }

    fun onTitleChanged(value: String) = _uiState.update { it.copy(titleInput = value, error = null) }
    fun onNoteChanged(value: String) = _uiState.update { it.copy(noteInput = value, error = null) }
    fun onTimeChanged(value: String) = _uiState.update { it.copy(timeInput = value.filter { char -> char.isDigit() || char == ':' }, error = null) }
    fun setKind(kind: AgendaItemKind) = _uiState.update { it.copy(kind = kind, error = null) }
    fun setProject(projectId: String?) = _uiState.update { it.copy(projectId = projectId, error = null) }

    fun addItem(toInbox: Boolean = _uiState.value.view == AgendaView.INBOX) {
        val state = _uiState.value
        val title = state.titleInput.trim()
        if (title.isBlank()) {
            _uiState.update { it.copy(error = "Escribe una acción para añadirla") }
            return
        }
        if (state.kind == AgendaItemKind.REMINDER && state.timeInput.isNotBlank() && !TIME.matches(state.timeInput)) {
            _uiState.update { it.copy(error = "La hora debe tener formato HH:mm") }
            return
        }
        val item = AgendaItem(
            title = title,
            note = state.noteInput.trim(),
            date = if (toInbox) null else state.selectedDate,
            time = state.timeInput,
            kind = state.kind,
            projectId = state.projectId
        )
        store.saveItem(item)
        scheduler.schedule(item)
        _uiState.update { it.copy(titleInput = "", noteInput = "", timeInput = "", error = null, message = "Añadido a la agenda") }
    }

    fun toggle(item: AgendaItem) = store.saveItem(item.copy(completed = !item.completed))

    fun remove(item: AgendaItem) {
        scheduler.cancel(item.id)
        store.removeItem(item.id)
        _uiState.update { it.copy(message = "Elemento eliminado") }
    }

    fun onProjectNameChanged(value: String) = _uiState.update { it.copy(projectNameInput = value, error = null) }
    fun onProjectGoalChanged(value: String) = _uiState.update { it.copy(projectGoalInput = value, error = null) }

    fun addProject() {
        val state = _uiState.value
        if (state.projectNameInput.isBlank()) {
            _uiState.update { it.copy(error = "Ponle un nombre al proyecto") }
            return
        }
        store.saveProject(AgendaProject(name = state.projectNameInput.trim(), goal = state.projectGoalInput.trim()))
        _uiState.update { it.copy(projectNameInput = "", projectGoalInput = "", error = null, message = "Proyecto creado") }
    }

    fun removeProject(project: AgendaProject) {
        store.removeProject(project.id)
        _uiState.update { it.copy(message = "Proyecto eliminado") }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null, error = null) }

    private fun moveDay(days: Long) {
        val date = LocalDate.parse(_uiState.value.selectedDate).plusDays(days)
        _uiState.update { it.copy(selectedDate = date.toString(), view = AgendaView.TODAY) }
    }

    private companion object { val TIME = Regex("(?:[01]\\d|2[0-3]):[0-5]\\d") }
}
