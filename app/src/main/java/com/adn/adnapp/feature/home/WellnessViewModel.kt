package com.adn.adnapp.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adn.adnapp.data.local.SunLog
import com.adn.adnapp.data.local.WaterLog
import com.adn.adnapp.data.local.WellnessStore
import com.adn.adnapp.data.model.entity.FoodEntry
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.FoodRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WellnessUiState(
    val date: String = LocalDate.now().toString(),
    val waterLogs: List<WaterLog> = emptyList(),
    val sunLogs: List<SunLog> = emptyList(),
    val waterInput: String = "250",
    val sunInput: String = "5",
    val waterGoalMl: Double = 2_000.0,
    val isSaving: Boolean = false,
    val error: String? = null,
    val message: String? = null
) {
    val waterTotalMl: Double get() = waterLogs.sumOf { it.amountMl }
    val sunTotalMinutes: Int get() = sunLogs.sumOf { it.minutes }
}

class WellnessViewModel(
    private val store: WellnessStore,
    private val authRepository: AuthRepository,
    private val foodRepository: FoodRepository,
    private val today: () -> LocalDate = { LocalDate.now() }
) : ViewModel() {
    private val ownerId = authRepository.getCurrentUserId() ?: LOCAL_OWNER
    private val _uiState = MutableStateFlow(WellnessUiState(date = today().toString()))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            store.data.collect { data ->
                _uiState.update { state ->
                    state.copy(
                        waterLogs = data.water.filter { it.ownerId == ownerId && it.date == state.date }
                            .sortedByDescending { it.createdAt },
                        sunLogs = data.sun.filter { it.ownerId == ownerId && it.date == state.date }
                            .sortedByDescending { it.createdAt }
                    )
                }
            }
        }
    }

    fun onWaterInputChanged(value: String) = _uiState.update { it.copy(waterInput = decimalInput(value), error = null) }

    fun onSunInputChanged(value: String) = _uiState.update { it.copy(sunInput = value.filter(Char::isDigit), error = null) }

    fun addQuickWater() = addWater(250.0)

    fun addWaterFromInput() {
        val amount = _uiState.value.waterInput.replace(',', '.').toDoubleOrNull()
        if (amount == null || !amount.isFinite() || amount <= 0.0) {
            _uiState.update { it.copy(error = "Introduce una cantidad de agua mayor que cero") }
            return
        }
        addWater(amount)
    }

    fun addWater(amount: Double) {
        if (_uiState.value.isSaving) return
        if (!amount.isFinite() || amount <= 0.0 || amount > 10_000.0) {
            _uiState.update { it.copy(error = "La cantidad debe estar entre 1 y 10.000 ml") }
            return
        }
        val log = WaterLog(ownerId = ownerId, date = _uiState.value.date, amountMl = amount)
        store.addWater(log)
        _uiState.update { it.copy(isSaving = true, error = null, message = null) }
        val uid = authRepository.getCurrentUserId()
        if (uid == null) {
            _uiState.update { it.copy(isSaving = false, message = "Agua registrada en este dispositivo") }
            return
        }
        viewModelScope.launch {
            foodRepository.saveWaterEntry(uid, log.date, amount).fold(
                onSuccess = { remoteId ->
                    store.attachRemoteEntry(log.id, remoteId)
                    _uiState.update { it.copy(isSaving = false, message = "Agua añadida al día") }
                },
                onFailure = {
                    _uiState.update { it.copy(isSaving = false, message = "Guardada localmente; se sincronizará al reintentar") }
                }
            )
        }
    }

    fun undoWater(log: WaterLog) {
        val uid = authRepository.getCurrentUserId()
        if (uid != null && !log.remoteEntryId.isNullOrBlank()) {
            _uiState.update { it.copy(isSaving = true, error = null) }
            viewModelScope.launch {
                foodRepository.deleteWaterEntry(uid, log.date, log.remoteEntryId).fold(
                    onSuccess = { removeWaterLog(log) },
                    onFailure = { _uiState.update { it.copy(isSaving = false, error = "No se pudo deshacer la entrada sincronizada") } }
                )
            }
        } else removeWaterLog(log)
    }

    fun addSunFromInput() {
        val minutes = _uiState.value.sunInput.toIntOrNull()
        if (minutes == null || minutes <= 0 || minutes > 720) {
            _uiState.update { it.copy(error = "Introduce entre 1 y 720 minutos") }
            return
        }
        addSun(minutes)
    }

    fun addSun(minutes: Int) {
        if (minutes !in 1..720) return
        store.addSun(SunLog(ownerId = ownerId, date = _uiState.value.date, minutes = minutes))
        _uiState.update { it.copy(error = null, message = "Tiempo exterior registrado") }
    }

    fun undoSun(log: SunLog) {
        store.removeSun(log.id)
        _uiState.update { it.copy(message = "Registro de sol deshecho") }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null, error = null) }

    private fun removeWaterLog(log: WaterLog) {
        store.removeWater(log.id)
        _uiState.update { it.copy(isSaving = false, message = "Entrada de agua deshecha") }
    }

    private fun decimalInput(value: String) = value.filterIndexed { index, char ->
        char.isDigit() || ((char == ',' || char == '.') && index > 0 && value.take(index).none { it == ',' || it == '.' })
    }

    private companion object {
        const val LOCAL_OWNER = "local-preview"
    }
}
