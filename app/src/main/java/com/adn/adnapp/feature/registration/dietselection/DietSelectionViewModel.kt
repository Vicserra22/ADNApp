package com.adn.adnapp.feature.registration.dietselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adn.adnapp.data.model.entity.Diet
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.DietRepository
import com.adn.adnapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DietSelectionUiState(
    val diets: List<Diet> = emptyList(),
    val selectedDietId: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)

sealed class DietSelectionEvent {
    object NavigateToMain : DietSelectionEvent()
}

class DietSelectionViewModel(
    private val dietRepository: DietRepository,
    private val userRepository: UserRepository,
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
            val result = dietRepository.getAvailableDiets()
            if (result.isSuccess) {
                _uiState.update { it.copy(diets = result.getOrNull() ?: emptyList(), isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Error al cargar dietas") }
            }
        }
    }

    fun onDietSelected(dietId: String) {
        _uiState.update { it.copy(selectedDietId = dietId) }
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

            val result = userRepository.updateField(uid, com.adn.adnapp.core.constants.FirestoreKeys.DIET, selectedId)
            if (result.isSuccess) {
                _uiState.update { it.copy(isSaving = false) }
                _eventFlow.emit(DietSelectionEvent.NavigateToMain)
            } else {
                _uiState.update { it.copy(isSaving = false, error = "Error al guardar dieta") }
            }
        }
    }
}
