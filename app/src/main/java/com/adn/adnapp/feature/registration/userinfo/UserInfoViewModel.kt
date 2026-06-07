package com.adn.adnapp.feature.registration.userinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adn.adnapp.data.model.entity.UserProfile
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserInfoUiState(
    val name: String = "",
    val age: String = "",
    val weight: String = "",
    val height: String = "",
    val gender: String = "Hombre",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class UserInfoEvent {
    object NavigateToDietSelection : UserInfoEvent()
}

class UserInfoViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserInfoUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UserInfoEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onNameChanged(name: String) = _uiState.update { it.copy(name = name) }
    fun onAgeChanged(age: String) = _uiState.update { it.copy(age = age) }
    fun onWeightChanged(weight: String) = _uiState.update { it.copy(weight = weight) }
    fun onHeightChanged(height: String) = _uiState.update { it.copy(height = height) }
    fun onGenderChanged(gender: String) = _uiState.update { it.copy(gender = gender) }

    fun onNextClicked() {
        val state = _uiState.value
        if (state.name.isBlank() || state.age.isBlank() || state.weight.isBlank() || state.height.isBlank()) {
            _uiState.update { it.copy(error = "Completa todos los campos") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val uid = authRepository.getCurrentUserId()
            if (uid == null) {
                _uiState.update { it.copy(isLoading = false, error = "Usuario no autenticado") }
                return@launch
            }

            val profile = UserProfile(
                uid = uid,
                name = state.name,
                age = state.age.toIntOrNull() ?: 0,
                weight = state.weight.toDoubleOrNull() ?: 0.0,
                height = state.height.toDoubleOrNull() ?: 0.0,
                gender = state.gender,
                dietId = "" // Set later in DietSelection
            )

            val result = userRepository.saveUserProfile(uid, profile)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false) }
                _eventFlow.emit(UserInfoEvent.NavigateToDietSelection)
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Error al guardar datos") }
            }
        }
    }
}
