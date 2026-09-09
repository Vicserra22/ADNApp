package com.adn.adnapp.feature.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adn.adnapp.data.local.HealthConnectLauncher
import com.adn.adnapp.data.local.RecoveryStore
import com.adn.adnapp.domain.model.HealthSource
import com.adn.adnapp.domain.model.SleepLog
import com.adn.adnapp.domain.model.StepLog
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecoveryUiState(
    val date: String = LocalDate.now().toString(), val stepsInput: String = "", val sleepInput: String = "",
    val steps: Int = 0, val sleepMinutes: Int = 0, val stepSource: HealthSource? = null, val sleepSource: HealthSource? = null,
    val connectionMessage: String = "Fuente no conectada", val error: String? = null, val message: String? = null
)

class RecoveryViewModel(private val store: RecoveryStore, private val launcher: HealthConnectLauncher) : ViewModel() {
    private val _uiState = MutableStateFlow(RecoveryUiState()); val uiState = _uiState.asStateFlow()
    init { viewModelScope.launch { store.data.collect { data -> _uiState.update { state -> val steps = data.steps.firstOrNull { it.date == state.date }; val sleep = data.sleep.firstOrNull { it.date == state.date }; state.copy(steps = steps?.steps ?: 0, sleepMinutes = sleep?.durationMinutes ?: 0, stepSource = steps?.source, sleepSource = sleep?.source) } } } }
    fun onDateChanged(value: String) = _uiState.update { it.copy(date = value, error = null) }
    fun onStepsChanged(value: String) = _uiState.update { it.copy(stepsInput = value.filter(Char::isDigit), error = null) }
    fun onSleepChanged(value: String) = _uiState.update { it.copy(sleepInput = value.filter { char -> char.isDigit() || char == '.' || char == ',' }, error = null) }
    fun saveSteps() { val state = _uiState.value; val date = parseDate(state.date) ?: return; val value = state.stepsInput.toIntOrNull(); if (value == null || value < 0) return fail("Introduce pasos válidos"); store.saveSteps(StepLog(date = date.toString(), steps = value)); _uiState.update { it.copy(stepsInput = "", message = "Pasos guardados", error = null) } }
    fun saveSleep() { val state = _uiState.value; val date = parseDate(state.date) ?: return; val hours = state.sleepInput.replace(',', '.').toDoubleOrNull(); if (hours == null || hours !in 0.1..24.0) return fail("El sueño debe estar entre 0,1 y 24 horas"); store.saveSleep(SleepLog(date = date.toString(), durationMinutes = (hours * 60).toInt())); _uiState.update { it.copy(sleepInput = "", message = "Sueño guardado", error = null) } }
    fun openHealthConnect() { _uiState.update { it.copy(connectionMessage = if (launcher.openSettings()) "Configuración de Health Connect abierta" else "Health Connect no está disponible en este dispositivo") } }
    fun clearMessage() = _uiState.update { it.copy(message = null, error = null) }
    private fun parseDate(value: String): LocalDate? = runCatching { LocalDate.parse(value) }.getOrElse { fail("La fecha debe tener formato AAAA-MM-DD"); null }
    private fun fail(message: String) { _uiState.update { it.copy(error = message, message = null) } }
}
