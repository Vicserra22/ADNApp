package com.adn.adnapp.feature.registration.dietselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adn.adnapp.data.model.entity.Diet
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.DietRepository
import com.adn.adnapp.domain.repository.NutritionRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class CustomDietDraft(
    val name: String = "",
    val description: String = "",
    val calories: String = "",
    val proteins: String = "",
    val carbs: String = "",
    val fats: String = "",
    val sugar: String = "50",
    val water: String = "2000"
)

data class DietSelectionUiState(
    val diets: List<Diet> = emptyList(),
    val selectedDietId: String? = null,
    val showCustomDietCreator: Boolean = false,
    val customDiet: CustomDietDraft = CustomDietDraft(),
    val isCreatingDiet: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)

sealed class DietSelectionEvent {
    object NavigateToMain : DietSelectionEvent()
}

class DietSelectionViewModel(
    private val dietRepository: DietRepository,
    private val nutritionRepository: NutritionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DietSelectionUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<DietSelectionEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadDiets()
    }

    private fun loadDiets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val uid = authRepository.getCurrentUserId()
            val result = dietRepository.getAvailableDiets(uid)
            if (result.isSuccess) {
                _uiState.update { it.copy(diets = result.getOrNull() ?: emptyList(), isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Error al cargar dietas") }
            }
        }
    }

    fun onRetryClicked() {
        loadDiets()
    }

    fun onDietSelected(dietId: String) {
        _uiState.update { it.copy(selectedDietId = dietId) }
    }

    fun showCustomDietCreator(show: Boolean) = _uiState.update {
        it.copy(showCustomDietCreator = show, customDiet = if (show) CustomDietDraft() else it.customDiet, error = null)
    }

    fun onCustomNameChanged(value: String) = updateDraft { copy(name = value) }
    fun onCustomDescriptionChanged(value: String) = updateDraft { copy(description = value) }
    fun onCustomCaloriesChanged(value: String) = updateDraft { copy(calories = value.decimalInput()) }
    fun onCustomProteinsChanged(value: String) = updateDraft { copy(proteins = value.decimalInput()) }
    fun onCustomCarbsChanged(value: String) = updateDraft { copy(carbs = value.decimalInput()) }
    fun onCustomFatsChanged(value: String) = updateDraft { copy(fats = value.decimalInput()) }
    fun onCustomSugarChanged(value: String) = updateDraft { copy(sugar = value.decimalInput()) }
    fun onCustomWaterChanged(value: String) = updateDraft { copy(water = value.decimalInput()) }

    fun saveCustomDiet() {
        val uid = authRepository.getCurrentUserId()
        if (uid == null) {
            _uiState.update { it.copy(error = "Usuario no autenticado") }
            return
        }
        val draft = _uiState.value.customDiet
        val calories = draft.calories.number()
        val proteins = draft.proteins.number()
        val carbs = draft.carbs.number()
        val fats = draft.fats.number()
        val sugar = draft.sugar.number()
        val water = draft.water.number()
        if (draft.name.trim().length < 3 || calories == null || calories !in 800.0..6_000.0 ||
            proteins == null || proteins !in 0.0..500.0 || carbs == null || carbs !in 0.0..800.0 ||
            fats == null || fats !in 0.0..300.0 || sugar == null || sugar !in 0.0..300.0 ||
            water == null || water !in 500.0..6_000.0
        ) {
            _uiState.update { it.copy(error = "Revisa el nombre y los objetivos diarios introducidos") }
            return
        }
        val diet = Diet(
            id = "custom_${UUID.randomUUID()}",
            name = draft.name.trim(),
            description = draft.description.trim().ifBlank { "Plan creado con tus propios objetivos diarios." },
            imageUrl = "custom",
            calories = calories,
            proteins = proteins,
            carbs = carbs,
            lipids = fats,
            sugar = sugar,
            water = water
        )
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingDiet = true, error = null) }
            dietRepository.saveCustomDiet(uid, diet).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            diets = it.diets + diet,
                            selectedDietId = diet.id,
                            showCustomDietCreator = false,
                            isCreatingDiet = false,
                            customDiet = CustomDietDraft()
                        )
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isCreatingDiet = false, error = "No se pudo guardar la dieta personalizada") }
                }
            )
        }
    }

    fun onCompleteClicked() {
        val selectedId = _uiState.value.selectedDietId
        if (selectedId == null) {
            _uiState.update { it.copy(error = "Selecciona una dieta") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val uid = authRepository.getCurrentUserId()
            if (uid == null) {
                _uiState.update { it.copy(isSaving = false, error = "Usuario no autenticado") }
                return@launch
            }

            val result = nutritionRepository.completeOnboarding(uid, selectedId)
            if (result.isSuccess) {
                _uiState.update { it.copy(isSaving = false) }
                _eventFlow.emit(DietSelectionEvent.NavigateToMain)
            } else {
                _uiState.update { it.copy(isSaving = false, error = "Error al guardar dieta") }
            }
        }
    }

    private inline fun updateDraft(transform: CustomDietDraft.() -> CustomDietDraft) {
        _uiState.update { it.copy(customDiet = transform(it.customDiet), error = null) }
    }
}

private fun String.number(): Double? = replace(',', '.').toDoubleOrNull()
private fun String.decimalInput(): String = filterIndexed { index, char ->
    char.isDigit() || ((char == ',' || char == '.') && index > 0 && take(index).none { it == ',' || it == '.' })
}
