package com.adn.adnapp.feature.sports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adn.adnapp.data.local.SportsStore
import com.adn.adnapp.domain.model.SportKind
import com.adn.adnapp.domain.model.SportSession
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SportsUiState(
    val selectedSport: SportKind = SportKind.RUN,
    val selectedDate: String = LocalDate.now().toString(),
    val sessions: List<SportSession> = emptyList(),
    val minutesInput: String = "",
    val distanceInput: String = "",
    val setsInput: String = "",
    val repsInput: String = "",
    val weightInput: String = "",
    val noteInput: String = "",
    val error: String? = null,
    val message: String? = null
) {
    val selectedSessions get() = sessions.filter { it.sport == selectedSport }
    val totalMinutes get() = selectedSessions.sumOf { it.minutes }
    val totalDistance get() = selectedSessions.sumOf { it.distanceKm }
    val bestDistance get() = selectedSessions.maxOfOrNull { it.distanceKm } ?: 0.0
    val lastSevenMinutes get() = selectedSessions.filter { it.date >= LocalDate.now().minusDays(6).toString() }.sumOf { it.minutes }
}

class SportsViewModel(private val store: SportsStore) : ViewModel() {
    private val _uiState = MutableStateFlow(SportsUiState())
    val uiState = _uiState.asStateFlow()

    init { viewModelScope.launch { store.data.collect { data -> _uiState.update { it.copy(sessions = data.sessions) } } } }

    fun selectSport(sport: SportKind) = _uiState.update { it.copy(selectedSport = sport, error = null, message = null) }
    fun onDateChanged(value: String) = _uiState.update { it.copy(selectedDate = value, error = null) }
    fun onMinutesChanged(value: String) = _uiState.update { it.copy(minutesInput = value.filter(Char::isDigit), error = null) }
    fun onDistanceChanged(value: String) = _uiState.update { it.copy(distanceInput = value.filter { it.isDigit() || it == '.' || it == ',' }, error = null) }
    fun onSetsChanged(value: String) = _uiState.update { it.copy(setsInput = value.filter(Char::isDigit), error = null) }
    fun onRepsChanged(value: String) = _uiState.update { it.copy(repsInput = value.filter(Char::isDigit), error = null) }
    fun onWeightChanged(value: String) = _uiState.update { it.copy(weightInput = value.filter { it.isDigit() || it == '.' || it == ',' }, error = null) }
    fun onNoteChanged(value: String) = _uiState.update { it.copy(noteInput = value, error = null) }

    fun addSession() {
        val state = _uiState.value
        val minutes = state.minutesInput.toIntOrNull()
        val date = runCatching { LocalDate.parse(state.selectedDate) }.getOrNull()
        if (minutes == null || minutes <= 0) return fail("Indica una duración mayor que cero")
        if (date == null) return fail("La fecha debe tener formato AAAA-MM-DD")
        val session = SportSession(
            sport = state.selectedSport, date = date.toString(), minutes = minutes,
            distanceKm = state.distanceInput.replace(',', '.').toDoubleOrNull() ?: 0.0,
            sets = state.setsInput.toIntOrNull() ?: 0, reps = state.repsInput.toIntOrNull() ?: 0,
            weightKg = state.weightInput.replace(',', '.').toDoubleOrNull() ?: 0.0,
            note = state.noteInput.trim()
        )
        store.save(session)
        _uiState.update { it.copy(minutesInput = "", distanceInput = "", setsInput = "", repsInput = "", weightInput = "", noteInput = "", error = null, message = "Sesión guardada") }
    }

    fun remove(session: SportSession) { store.remove(session.id); _uiState.update { it.copy(message = "Sesión eliminada") } }
    fun clearMessage() = _uiState.update { it.copy(message = null, error = null) }
    private fun fail(message: String) = _uiState.update { it.copy(error = message, message = null) }
}
