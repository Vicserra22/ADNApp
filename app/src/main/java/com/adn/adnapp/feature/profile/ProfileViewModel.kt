package com.adn.adnapp.feature.profile

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

data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed class ProfileEvent {
    object NavigateToSplash : ProfileEvent()
}

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ProfileEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val uid = authRepository.getCurrentUserId()
            if (uid != null) {
                val result = userRepository.getUserProfile(uid)
                if (result.isSuccess) {
                    _uiState.update { it.copy(userProfile = result.getOrNull(), isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Error al cargar perfil") }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "No autenticado") }
            }
        }
    }

    fun onLogoutClicked() {
        authRepository.logout()
        viewModelScope.launch {
            _eventFlow.emit(ProfileEvent.NavigateToSplash)
        }
    }
}
