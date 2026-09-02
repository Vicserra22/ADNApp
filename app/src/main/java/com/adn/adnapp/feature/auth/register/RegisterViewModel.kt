package com.adn.adnapp.feature.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adn.adnapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class RegisterEvent {
    object NavigateToUserInfo : RegisterEvent()
    object NavigateBack : RegisterEvent()
}

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<RegisterEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onRegisterClicked(password: String) {
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
            val result = authRepository.register(_uiState.value.email, password)
            if (result.isSuccess) {
                _eventFlow.emit(RegisterEvent.NavigateToUserInfo)
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Error al registrarse") }
            }
        }
    }

    fun onBackClicked() {
        viewModelScope.launch {
            _eventFlow.emit(RegisterEvent.NavigateBack)
        }
    }

    private fun isValidEmail(value: String): Boolean {
        val at = value.indexOf('@')
        return at > 0 && value.indexOf('.', startIndex = at + 2) > at + 1
    }
}
