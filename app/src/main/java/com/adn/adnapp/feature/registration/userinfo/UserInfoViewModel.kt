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
    val name: String = "", val age: String = "", val weight: String = "",
    val height: String = "", val gender: String = "Hombre",
    val isLoading: Boolean = false, val error: String? = null
)

sealed class UserInfoEvent { data object NavigateToDietSelection : UserInfoEvent() }

class UserInfoViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UserInfoUiState())
    val uiState = _uiState.asStateFlow()
    private val _eventFlow = MutableSharedFlow<UserInfoEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init { loadExistingProfile() }

    fun onNameChanged(value: String) = update { copy(name = value, error = null) }
    fun onAgeChanged(value: String) = update { copy(age = value, error = null) }
    fun onWeightChanged(value: String) = update { copy(weight = value, error = null) }
    fun onHeightChanged(value: String) = update { copy(height = value, error = null) }
    fun onGenderChanged(value: String) = update { copy(gender = value, error = null) }

    fun onNextClicked() {
        val state = _uiState.value
        val age = state.age.toIntOrNull()
        val weight = state.weight.replace(',', '.').toDoubleOrNull()
        val height = state.height.replace(',', '.').toDoubleOrNull()
        if (state.name.isBlank() || age == null || weight == null || height == null) {
            update { copy(error = "Completa todos los campos con valores válidos") }
            return
        }
        if (age !in 13..120 || weight !in 20.0..400.0 || height !in 80.0..250.0) {
            update { copy(error = "Revisa la edad, el peso y la altura") }
            return
        }
        viewModelScope.launch {
            update { copy(isLoading = true, error = null) }
            val uid = authRepository.getCurrentUserId()
            if (uid == null) {
                update { copy(isLoading = false, error = "Usuario no autenticado") }
                return@launch
            }
            val profile = UserProfile(uid, state.name.trim(), age, weight, height, state.gender)
            userRepository.saveUserProfile(uid, profile).fold(
                onSuccess = {
                    update { copy(isLoading = false) }
                    _eventFlow.emit(UserInfoEvent.NavigateToDietSelection)
                },
                onFailure = { update { copy(isLoading = false, error = "Error al guardar datos") } }
            )
        }
    }

    private fun loadExistingProfile() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            userRepository.getUserProfile(uid).getOrNull()?.let { profile ->
                update {
                    copy(name = profile.name, age = profile.age.toString(),
                        weight = profile.weight.toString(), height = profile.height.toString(),
                        gender = profile.gender)
                }
            }
        }
    }

    private inline fun update(transform: UserInfoUiState.() -> UserInfoUiState) {
        _uiState.update(transform)
    }
}
