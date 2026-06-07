package com.adn.adnapp.feature.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adn.adnapp.domain.repository.AuthRepository
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
    object NavigateToRegistrationFlow : LoginEvent()
    object NavigateBack : LoginEvent()
}

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
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

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.login(_uiState.value.email, password)
            if (result.isSuccess) {
                val uid = authRepository.getCurrentUserId()
                if (uid != null) {
                    val userExistsResult = userRepository.userExists(uid)
                    if (userExistsResult.isSuccess && userExistsResult.getOrNull() == true) {
                        _eventFlow.emit(LoginEvent.NavigateToMain)
                    } else {
                        _eventFlow.emit(LoginEvent.NavigateToRegistrationFlow)
                    }
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
}
