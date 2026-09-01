package com.adn.adnapp.feature.dayviewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DayViewerUiState(
    val date: String,
    val consumption: DailyConsumption = DailyConsumption(date = date),
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val calories: String = "",
    val proteins: String = "",
    val carbs: String = "",
    val fats: String = "",
    val error: String? = null,
    val savedMessage: String? = null
)

class DayViewerViewModel(
    private val date: String,
    private val foodRepository: FoodRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DayViewerUiState(date = date))
    val uiState = _uiState.asStateFlow()

    init { observeDay() }

    fun startEditing() {
        val daily = _uiState.value.consumption
        _uiState.update {
            it.copy(
                isEditing = true, error = null, savedMessage = null,
                calories = daily.calories.editValue(), proteins = daily.proteins.editValue(),
                carbs = daily.carbs.editValue(), fats = daily.fats.editValue()
            )
        }
    }

    fun cancelEditing() = _uiState.update { it.copy(isEditing = false, error = null) }
    fun onCaloriesChanged(value: String) = change { copy(calories = value, error = null) }
    fun onProteinsChanged(value: String) = change { copy(proteins = value, error = null) }
    fun onCarbsChanged(value: String) = change { copy(carbs = value, error = null) }
    fun onFatsChanged(value: String) = change { copy(fats = value, error = null) }

    fun saveManualMacros() {
        val state = _uiState.value
        val calories = state.calories.numberOrNull()
        val proteins = state.proteins.numberOrNull()
        val carbs = state.carbs.numberOrNull()
        val fats = state.fats.numberOrNull()
        if (listOf(calories, proteins, carbs, fats).any { it == null || it < 0.0 }) {
            _uiState.update { it.copy(error = "Introduce valores numéricos iguales o mayores que cero") }
            return
        }
        val uid = authRepository.getCurrentUserId()
        if (uid == null) {
            _uiState.update { it.copy(error = "Usuario no autenticado") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val updated = DailyConsumption(
                date = date,
                calories = calories!!,
                proteins = proteins!!,
                carbs = carbs!!,
                fats = fats!!,
                sugar = state.consumption.sugar
            )
            foodRepository.setDailyConsumption(uid, updated).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(consumption = updated, isSaving = false, isEditing = false,
                            savedMessage = "Macros guardados")
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isSaving = false, error = "No se pudieron guardar los macros") }
                }
            )
        }
    }

    private fun observeDay() {
        val uid = authRepository.getCurrentUserId()
        if (uid == null) {
            _uiState.update { it.copy(isLoading = false, error = "Usuario no autenticado") }
            return
        }
        viewModelScope.launch {
            foodRepository.observeDailyConsumption(uid, date)
                .catch { _uiState.update { state -> state.copy(isLoading = false, error = "No se pudo cargar el día") } }
                .collect { daily -> _uiState.update { it.copy(consumption = daily, isLoading = false) } }
        }
    }

    private inline fun change(transform: DayViewerUiState.() -> DayViewerUiState) {
        _uiState.update(transform)
    }
}

private fun String.numberOrNull() = replace(',', '.').toDoubleOrNull()
private fun Double.editValue() = if (this == 0.0) "" else toString()
