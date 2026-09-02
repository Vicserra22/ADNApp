package com.adn.adnapp.feature.registration.priorities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adn.adnapp.domain.model.BodyGoal
import com.adn.adnapp.domain.model.Importance
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PrioritiesUiState(
    val bodyGoal: BodyGoal = BodyGoal.MAINTAIN,
    val targetWeight: String = "",
    val nutrition: Importance = Importance.NORMAL,
    val sports: Importance = Importance.NORMAL,
    val goals: Importance = Importance.NORMAL,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)

class PrioritiesViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PrioritiesUiState())
    val uiState = _uiState.asStateFlow()
    private val _completed = MutableSharedFlow<Unit>()
    val completed = _completed.asSharedFlow()

    init { load() }

    fun setBodyGoal(value: BodyGoal) = _uiState.update { it.copy(bodyGoal = value) }
    fun setTargetWeight(value: String) = _uiState.update { it.copy(targetWeight = value, error = null) }
    fun setNutrition(value: Importance) = _uiState.update { it.copy(nutrition = value) }
    fun setSports(value: Importance) = _uiState.update { it.copy(sports = value) }
    fun setGoals(value: Importance) = _uiState.update { it.copy(goals = value) }

    fun save() {
        val uid = authRepository.getCurrentUserId() ?: return
        val target = _uiState.value.targetWeight.replace(',', '.').toDoubleOrNull()
        if (target == null || target !in 30.0..400.0) {
            _uiState.update { it.copy(error = "Introduce un peso objetivo válido") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val profile = userRepository.getUserProfile(uid).getOrNull()
            if (profile == null) {
                _uiState.update { it.copy(isSaving = false, error = "No se pudo cargar el perfil") }
                return@launch
            }
            val state = _uiState.value
            userRepository.saveUserProfile(
                uid,
                profile.copy(
                    targetWeight = target, bodyGoal = state.bodyGoal,
                    nutritionImportance = state.nutrition, sportsImportance = state.sports,
                    goalsImportance = state.goals, prioritiesCompleted = true
                )
            ).fold(
                onSuccess = { _uiState.update { it.copy(isSaving = false) }; _completed.emit(Unit) },
                onFailure = { _uiState.update { it.copy(isSaving = false, error = "No se pudo guardar") } }
            )
        }
    }

    private fun load() {
        val uid = authRepository.getCurrentUserId() ?: run {
            _uiState.update { it.copy(isLoading = false, error = "Usuario no autenticado") }
            return
        }
        viewModelScope.launch {
            val profile = userRepository.getUserProfile(uid).getOrNull()
            _uiState.update {
                it.copy(
                    bodyGoal = profile?.bodyGoal ?: BodyGoal.MAINTAIN,
                    targetWeight = (profile?.targetWeight?.takeIf { weight -> weight > 0 }
                        ?: profile?.weight)?.toString().orEmpty(),
                    nutrition = profile?.nutritionImportance ?: Importance.NORMAL,
                    sports = profile?.sportsImportance ?: Importance.NORMAL,
                    goals = profile?.goalsImportance ?: Importance.NORMAL,
                    isLoading = false
                )
            }
        }
    }
}
