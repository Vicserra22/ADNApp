package com.adn.adnapp.feature.dayviewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.data.model.entity.FoodEntry
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.DietRepository
import com.adn.adnapp.domain.repository.FoodRepository
import com.adn.adnapp.domain.repository.NutritionRepository
import com.adn.adnapp.domain.repository.UserRepository
import com.adn.adnapp.domain.model.DayScore
import com.adn.adnapp.domain.model.NutritionTargets
import com.adn.adnapp.domain.service.GoalCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DayViewerUiState(
    val date: String,
    val consumption: DailyConsumption = DailyConsumption(date = date),
    val entries: List<FoodEntry> = emptyList(),
    val targets: NutritionTargets? = null,
    val score: DayScore? = null,
    val isLoading: Boolean = true,
    val areEntriesLoading: Boolean = true,
    val isEditing: Boolean = false,
    val editingEntry: FoodEntry? = null,
    val isSaving: Boolean = false,
    val deletingEntryId: String? = null,
    val name: String = "",
    val quantity: String = "",
    val calories: String = "",
    val proteins: String = "",
    val carbs: String = "",
    val fats: String = "",
    val sugar: String = "",
    val waterMl: String = "250",
    val error: String? = null,
    val savedMessage: String? = null
)

class DayViewerViewModel(
    private val date: String,
    private val foodRepository: FoodRepository,
    private val authRepository: AuthRepository,
    private val nutritionRepository: NutritionRepository,
    private val dietRepository: DietRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DayViewerUiState(date = date))
    val uiState = _uiState.asStateFlow()

    init {
        observeDay()
        observeEntries()
        loadGoals()
    }

    /** Saving this empty form creates a separate manual entry; it never overwrites the day. */
    fun startEditing() = _uiState.update {
        it.copy(
            isEditing = true, editingEntry = null, name = "Ajuste manual", quantity = "",
            calories = "", proteins = "", carbs = "", fats = "", sugar = "",
            error = null, savedMessage = null
        )
    }

    fun startEditingEntry(entry: FoodEntry) = _uiState.update {
        it.copy(
            isEditing = true, editingEntry = entry, name = entry.name,
            quantity = entry.quantity.editValue(), calories = entry.calories.editValue(),
            proteins = entry.proteins.editValue(), carbs = entry.carbs.editValue(),
            fats = entry.fats.editValue(), sugar = entry.sugar.editValue(),
            waterMl = entry.waterMl.editValue(), error = null, savedMessage = null
        )
    }

    fun cancelEditing() = _uiState.update {
        it.copy(isEditing = false, editingEntry = null, error = null, waterMl = "250")
    }

    fun onNameChanged(value: String) = change { copy(name = value, error = null) }
    fun onQuantityChanged(value: String) = change { copy(quantity = value, error = null) }
    fun onCaloriesChanged(value: String) = change { copy(calories = value, error = null) }
    fun onProteinsChanged(value: String) = change { copy(proteins = value, error = null) }
    fun onCarbsChanged(value: String) = change { copy(carbs = value, error = null) }
    fun onFatsChanged(value: String) = change { copy(fats = value, error = null) }
    fun onSugarChanged(value: String) = change { copy(sugar = value, error = null) }
    fun onWaterChanged(value: String) = change { copy(waterMl = value, error = null) }

    fun saveManualMacros() {
        val state = _uiState.value
        val values = listOf(state.calories, state.proteins, state.carbs, state.fats, state.sugar)
            .map { it.numberOrZero() }
        if (values.any { it == null || it < 0.0 }) {
            _uiState.update { it.copy(error = "Introduce valores numéricos iguales o mayores que cero") }
            return
        }
        val water = if (state.editingEntry?.kind == FoodEntry.KIND_WATER) state.waterMl.numberOrZero() else 0.0
        if (water == null || water < 0.0 || (values.all { it == 0.0 } && water == 0.0)) {
            _uiState.update { it.copy(error = "Añade al menos una cantidad mayor que cero") }
            return
        }
        val existing = state.editingEntry
        val entry = if (existing == null) {
            FoodEntry(
                name = state.name.ifBlank { "Ajuste manual" }, calories = values[0]!!,
                proteins = values[1]!!, carbs = values[2]!!, fats = values[3]!!,
                sugar = values[4]!!, kind = FoodEntry.KIND_MANUAL
            )
        } else {
            existing.copy(
                name = state.name.ifBlank { existing.name },
                quantity = state.quantity.numberOrZero() ?: existing.quantity,
                calories = values[0]!!, proteins = values[1]!!, carbs = values[2]!!,
                fats = values[3]!!, sugar = values[4]!!, waterMl = water
            )
        }
        persistEntry(entry, isNew = existing == null)
    }

    fun addWater() {
        val amount = _uiState.value.waterMl.numberOrNull()
        if (amount == null || amount <= 0.0) {
            _uiState.update { it.copy(error = "Introduce una cantidad de agua mayor que cero") }
            return
        }
        persistEntry(
            FoodEntry(name = "Agua", waterMl = amount, kind = FoodEntry.KIND_WATER),
            isNew = true, successMessage = "Agua añadida"
        )
    }

    fun deleteEntry(entry: FoodEntry) {
        val uid = currentUid() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(deletingEntryId = entry.id, error = null, savedMessage = null) }
            foodRepository.deleteFoodEntry(uid, entry.id, date).fold(
                onSuccess = { _uiState.update { it.copy(deletingEntryId = null, savedMessage = "Entrada eliminada") } },
                onFailure = { _uiState.update { it.copy(deletingEntryId = null, error = "No se pudo eliminar la entrada") } }
            )
        }
    }

    private fun persistEntry(entry: FoodEntry, isNew: Boolean, successMessage: String = "Entrada guardada") {
        val uid = currentUid() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, savedMessage = null) }
            val result = if (isNew) foodRepository.saveFoodEntry(uid, entry, date)
            else foodRepository.updateFoodEntry(uid, entry, date)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(isSaving = false, isEditing = false, editingEntry = null,
                            waterMl = "250", savedMessage = successMessage)
                    }
                },
                onFailure = { _uiState.update { it.copy(isSaving = false, error = "No se pudo guardar la entrada") } }
            )
        }
    }

    private fun observeDay() {
        val uid = currentUid() ?: return
        viewModelScope.launch {
            foodRepository.observeDailyConsumption(uid, date)
                .catch { _uiState.update { state -> state.copy(isLoading = false, error = "No se pudo cargar el día") } }
                .collect { daily -> _uiState.update {
                    it.copy(
                        consumption = daily,
                        score = if (it.targets != null) calculateScore(daily, it.targets) else null,
                        isLoading = false
                    )
                } }
        }
    }

    private fun loadGoals() {
        val uid = currentUid() ?: return
        viewModelScope.launch {
            val profile = userRepository.getUserProfile(uid).getOrNull() ?: return@launch
            val nutrition = nutritionRepository.getNutritionProfile(uid).getOrNull() ?: return@launch
            val diet = dietRepository.getDiet(nutrition.dietId).getOrNull() ?: return@launch
            val targets = GoalCalculator.targets(profile, diet)
            scoreProfile = profile
            _uiState.update {
                it.copy(targets = targets, score = GoalCalculator.score(it.consumption, targets, profile))
            }
        }
    }

    private var scoreProfile: com.adn.adnapp.data.model.entity.UserProfile? = null

    private fun calculateScore(consumption: DailyConsumption, targets: NutritionTargets): DayScore? =
        scoreProfile?.let { GoalCalculator.score(consumption, targets, it) }

    private fun observeEntries() {
        val uid = currentUid() ?: return
        viewModelScope.launch {
            foodRepository.observeFoodEntries(uid, date)
                .catch { _uiState.update { state -> state.copy(areEntriesLoading = false, error = "No se pudieron cargar las entradas") } }
                .collect { entries -> _uiState.update { it.copy(entries = entries, areEntriesLoading = false) } }
        }
    }

    private fun currentUid(): String? = authRepository.getCurrentUserId().also { uid ->
        if (uid == null) _uiState.update {
            it.copy(isLoading = false, areEntriesLoading = false, error = "Usuario no autenticado")
        }
    }

    private inline fun change(transform: DayViewerUiState.() -> DayViewerUiState) = _uiState.update(transform)
}

private fun String.numberOrNull() = replace(',', '.').toDoubleOrNull()
private fun String.numberOrZero() = if (isBlank()) 0.0 else numberOrNull()
private fun Double.editValue() = if (this == 0.0) "" else toString()
