package com.adn.adnapp.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.NutritionRepository
import com.adn.adnapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SplashDestination {
    WELCOME,
    USER_INFO,
    DIET_SELECTION,
    MAIN
}

data class SplashUiState(
    val isLoading: Boolean = true,
    val destination: SplashDestination? = null,
    val error: String? = null
)

class SplashViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val nutritionRepository: NutritionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState = _uiState.asStateFlow()

    fun onAppStarted() {
        viewModelScope.launch {
            _uiState.value = SplashUiState(isLoading = true)
            if (!authRepository.isAuthenticated()) {
                navigateTo(SplashDestination.WELCOME)
                return@launch
            }
            val uid = authRepository.getCurrentUserId()
            if (uid == null) {
                navigateTo(SplashDestination.WELCOME)
                return@launch
            }
            val userExists = userRepository.userExists(uid).getOrElse {
                showError()
                return@launch
            }
            if (!userExists) {
                navigateTo(SplashDestination.USER_INFO)
                return@launch
            }
            val nutritionProfile = nutritionRepository.getNutritionProfile(uid).getOrElse {
                showError()
                return@launch
            }
            navigateTo(
                if (nutritionProfile?.onboardingCompleted == true) {
                    SplashDestination.MAIN
                } else {
                    SplashDestination.DIET_SELECTION
                }
            )
        }
    }

    private fun navigateTo(destination: SplashDestination) {
        _uiState.update { it.copy(isLoading = false, destination = destination, error = null) }
    }

    private fun showError() {
        _uiState.update {
            it.copy(
                isLoading = false,
                error = "No se pudo comprobar tu sesión. Revisa la conexión e inténtalo de nuevo."
            )
        }
    }
}
