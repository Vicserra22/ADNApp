package com.adn.adnapp.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DashboardUiState(
    val history: Map<String, DailyConsumption> = emptyMap(),
    val selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
)

class DashboardViewModel(
    private val foodRepository: FoodRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeHistory()
    }

    private fun observeHistory() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            foodRepository.observeConsumptionHistory(uid).collect { history ->
                _uiState.update { it.copy(history = history) }
            }
        }
    }

    fun onDateSelected(dateStr: String) {
        _uiState.update { it.copy(selectedDate = dateStr) }
    }
}
