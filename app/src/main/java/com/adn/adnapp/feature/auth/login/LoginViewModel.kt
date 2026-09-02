package com.adn.adnapp.feature.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.NutritionRepository
import com.adn.adnapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class LoginEvent {
    object NavigateToMain : LoginEvent()
    object NavigateToUserInfo : LoginEvent()
    object NavigateToDietSelection : LoginEvent()
    object NavigateToPriorities : LoginEvent()
    object NavigateBack : LoginEvent()
}

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val nutritionRepository: NutritionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<LoginEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onLoginClicked(password: String) {
        if (_uiState.value.email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Completa todos los campos") }
            return
        }
        if (!isValidEmail(_uiState.value.email) || password.length < 6) {
            _uiState.update { it.copy(error = "Introduce un email válido y una contraseña de al menos 6 caracteres") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.login(_uiState.value.email, password)
            if (result.isSuccess) {
                val uid = authRepository.getCurrentUserId()
                if (uid != null) {
                    val userExists = userRepository.userExists(uid).getOrElse {
                        _uiState.update { state -> state.copy(isLoading = false, error = "No se pudo comprobar el perfil") }
                        return@launch
                    }
                    if (!userExists) {
                        _eventFlow.emit(LoginEvent.NavigateToUserInfo)
                        return@launch
                    }
                    val userProfile = userRepository.getUserProfile(uid).getOrElse {
                        _uiState.update { state -> state.copy(isLoading = false, error = "No se pudo cargar el perfil") }
                        return@launch
                    }
                    if (userProfile?.prioritiesCompleted != true) {
                        _eventFlow.emit(LoginEvent.NavigateToPriorities)
                        return@launch
                    }
                    val nutritionProfile = nutritionRepository.getNutritionProfile(uid).getOrElse {
                        _uiState.update { state -> state.copy(isLoading = false, error = "No se pudo comprobar Nutrición") }
                        return@launch
                    }
                    _eventFlow.emit(
                        if (nutritionProfile?.onboardingCompleted == true) {
                            LoginEvent.NavigateToMain
                        } else {
                            LoginEvent.NavigateToDietSelection
                        }
                    )
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Error al obtener usuario") }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Error al iniciar sesión") }
            }
        }
    }

    fun onBackClicked() {
        viewModelScope.launch {
            _eventFlow.emit(LoginEvent.NavigateBack)
        }
    }

    private fun isValidEmail(value: String): Boolean {
        val at = value.indexOf('@')
        return at > 0 && value.indexOf('.', startIndex = at + 2) > at + 1
    }
}
