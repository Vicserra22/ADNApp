package com.adn.adnapp.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adn.adnapp.data.model.entity.Diet
import com.adn.adnapp.data.model.entity.UserProfile
import com.adn.adnapp.data.model.entity.WeightEntry
import com.adn.adnapp.domain.model.BodyGoal
import com.adn.adnapp.domain.model.Importance
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.DietRepository
import com.adn.adnapp.domain.repository.NutritionRepository
import com.adn.adnapp.domain.repository.UserRepository
import com.adn.adnapp.domain.repository.WeightRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val diets: List<Diet> = emptyList(),
    val selectedDietId: String = "",
    val weightHistory: List<WeightEntry> = emptyList(),
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isAddingWeight: Boolean = false,
    val name: String = "",
    val age: String = "",
    val weight: String = "",
    val height: String = "",
    val gender: String = "Hombre",
    val targetWeight: String = "",
    val bodyGoal: BodyGoal = BodyGoal.MAINTAIN,
    val nutritionImportance: Importance = Importance.NORMAL,
    val sportsImportance: Importance = Importance.NORMAL,
    val goalsImportance: Importance = Importance.NORMAL,
    val newWeight: String = "",
    val newWeightDate: String = LocalDate.now().toString(),
    val error: String? = null,
    val message: String? = null
)

sealed class ProfileEvent { data object NavigateToSplash : ProfileEvent() }

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val weightRepository: WeightRepository,
    private val nutritionRepository: NutritionRepository,
    private val dietRepository: DietRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()
    private val _eventFlow = MutableSharedFlow<ProfileEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init { loadProfile() }

    fun startEditing() = _uiState.update { it.copy(isEditing = true, error = null, message = null) }
    fun cancelEditing() = _uiState.update { state ->
        state.userProfile?.let { state.withProfile(it).copy(isEditing = false, error = null) }
            ?: state.copy(isEditing = false)
    }
    fun onNameChanged(value: String) = formUpdate { copy(name = value) }
    fun onAgeChanged(value: String) = formUpdate { copy(age = value.filter(Char::isDigit)) }
    fun onWeightChanged(value: String) = formUpdate { copy(weight = value.decimalInput()) }
    fun onHeightChanged(value: String) = formUpdate { copy(height = value.decimalInput()) }
    fun onTargetWeightChanged(value: String) = formUpdate { copy(targetWeight = value.decimalInput()) }
    fun onGenderChanged(value: String) = formUpdate { copy(gender = value) }
    fun onBodyGoalChanged(value: BodyGoal) = formUpdate { copy(bodyGoal = value) }
    fun onNutritionImportanceChanged(value: Importance) = formUpdate { copy(nutritionImportance = value) }
    fun onSportsImportanceChanged(value: Importance) = formUpdate { copy(sportsImportance = value) }
    fun onGoalsImportanceChanged(value: Importance) = formUpdate { copy(goalsImportance = value) }
    fun onDietChanged(value: String) = formUpdate { copy(selectedDietId = value) }
    fun onNewWeightChanged(value: String) = formUpdate { copy(newWeight = value.decimalInput()) }
    fun onNewWeightDateChanged(value: String) = formUpdate { copy(newWeightDate = value) }
    fun clearMessage() = _uiState.update { it.copy(message = null) }

    fun saveProfile() {
        val state = _uiState.value
        val age = state.age.toIntOrNull()
        val weight = state.weight.toDoubleValue()
        val height = state.height.toDoubleValue()
        val targetWeight = state.targetWeight.toDoubleValue()
        if (state.name.isBlank() || age == null || age !in 13..120 ||
            weight == null || weight !in 20.0..400.0 ||
            height == null || height !in 80.0..250.0 ||
            targetWeight == null || targetWeight !in 20.0..400.0
        ) {
            _uiState.update { it.copy(error = "Revisa el nombre, la edad, el peso, la altura y el peso objetivo") }
            return
        }
        val uid = authRepository.getCurrentUserId() ?: return showAuthError()
        val previous = state.userProfile ?: return
        val updated = previous.copy(
            name = state.name.trim(), age = age!!, weight = weight!!, height = height!!,
            gender = state.gender, targetWeight = targetWeight!!, bodyGoal = state.bodyGoal,
            nutritionImportance = state.nutritionImportance,
            sportsImportance = state.sportsImportance, goalsImportance = state.goalsImportance
        )
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, message = null) }
            val profileResult = userRepository.saveUserProfile(uid, updated)
            val dietResult = if (state.selectedDietId.isBlank()) Result.success(Unit)
                else nutritionRepository.updateDiet(uid, state.selectedDietId)
            if (profileResult.isSuccess && dietResult.isSuccess) {
                if (previous.weight != updated.weight) {
                    weightRepository.saveWeight(uid, WeightEntry(LocalDate.now().toString(), updated.weight))
                }
                _uiState.update {
                    it.withProfile(updated).copy(
                        isSaving = false, isEditing = false, message = "Perfil actualizado"
                    )
                }
            } else {
                _uiState.update { it.copy(isSaving = false, error = "No se pudo actualizar todo el perfil") }
            }
        }
    }

    fun addWeight() {
        val state = _uiState.value
        val kilograms = state.newWeight.toDoubleValue()
        val date = runCatching { LocalDate.parse(state.newWeightDate) }.getOrNull()
        if (kilograms == null || kilograms !in 20.0..400.0 || date == null || date.isAfter(LocalDate.now())) {
            _uiState.update { it.copy(error = "Introduce un peso válido y una fecha no futura") }
            return
        }
        val uid = authRepository.getCurrentUserId() ?: return showAuthError()
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingWeight = true, error = null, message = null) }
            weightRepository.saveWeight(uid, WeightEntry(date.toString(), kilograms!!)).fold(
                onSuccess = {
                    if (date == LocalDate.now()) {
                        _uiState.value.userProfile?.copy(weight = kilograms)?.let { updated ->
                            userRepository.saveUserProfile(uid, updated)
                            _uiState.update { it.withProfile(updated) }
                        }
                    }
                    _uiState.update {
                        it.copy(isAddingWeight = false, newWeight = "", message = "Peso registrado")
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isAddingWeight = false, error = "No se pudo registrar el peso") }
                }
            )
        }
    }

    fun retry() = loadProfile()
    fun onLogoutClicked() {
        authRepository.logout()
        viewModelScope.launch { _eventFlow.emit(ProfileEvent.NavigateToSplash) }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val uid = authRepository.getCurrentUserId()
            if (uid == null) {
                showAuthError()
                return@launch
            }
            val profileResult = userRepository.getUserProfile(uid)
            if (profileResult.isFailure || profileResult.getOrNull() == null) {
                _uiState.update { it.copy(isLoading = false, error = "Error al cargar el perfil") }
                return@launch
            }
            val profile = requireNotNull(profileResult.getOrNull())
            val diets = dietRepository.getAvailableDiets().getOrDefault(emptyList())
            val nutrition = nutritionRepository.getNutritionProfile(uid).getOrNull()
            _uiState.update {
                it.withProfile(profile).copy(
                    diets = diets, selectedDietId = nutrition?.dietId.orEmpty(), isLoading = false
                )
            }
            weightRepository.observeWeightHistory(uid)
                .catch { _uiState.update { it.copy(error = "No se pudo cargar el historial de peso") } }
                .collect { history -> _uiState.update { it.copy(weightHistory = history) } }
        }
    }

    private fun showAuthError() {
        _uiState.update { it.copy(isLoading = false, isSaving = false, error = "Usuario no autenticado") }
    }
    private inline fun formUpdate(transform: ProfileUiState.() -> ProfileUiState) {
        _uiState.update { transform(it).copy(error = null, message = null) }
    }
}

private fun ProfileUiState.withProfile(profile: UserProfile) = copy(
    userProfile = profile, name = profile.name, age = profile.age.toString(),
    weight = profile.weight.displayNumber(), height = profile.height.displayNumber(),
    gender = profile.gender.ifBlank { "Hombre" },
    targetWeight = (profile.targetWeight.takeIf { it > 0 } ?: profile.weight).displayNumber(),
    bodyGoal = profile.bodyGoal, nutritionImportance = profile.nutritionImportance,
    sportsImportance = profile.sportsImportance, goalsImportance = profile.goalsImportance
)

private fun String.decimalInput(): String = filterIndexed { index, char ->
    char.isDigit() || ((char == ',' || char == '.') && index > 0 &&
        take(index).none { it == ',' || it == '.' })
}
private fun String.toDoubleValue(): Double? = replace(',', '.').toDoubleOrNull()
private fun Double.displayNumber(): String = if (this % 1.0 == 0.0) toInt().toString() else toString()
