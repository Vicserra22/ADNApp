package com.adn.adnapp.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SplashViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<SplashNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun onAppStarted() {
        viewModelScope.launch {
            if (authRepository.isAuthenticated()) {
                val uid = authRepository.getCurrentUserId()
                if (uid != null) {
                    val userExistsResult = userRepository.userExists(uid)
                    if (userExistsResult.isSuccess && userExistsResult.getOrNull() == true) {
                        _navigationEvent.emit(SplashNavigationEvent.NavigateToMain)
                    } else {
                        _navigationEvent.emit(SplashNavigationEvent.NavigateToRegistration)
                    }
                } else {
                    _navigationEvent.emit(SplashNavigationEvent.NavigateToWelcome)
                }
            } else {
                _navigationEvent.emit(SplashNavigationEvent.NavigateToWelcome)
            }
        }
    }
}

sealed class SplashNavigationEvent {
    object NavigateToWelcome : SplashNavigationEvent()
    object NavigateToMain : SplashNavigationEvent()
    object NavigateToRegistration : SplashNavigationEvent()
}
