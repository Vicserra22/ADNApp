package com.adn.adnapp.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adn.adnapp.data.local.DashboardPreferences
import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.domain.model.AppArea
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.FoodRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val history: Map<String, DailyConsumption> = emptyMap(),
    val selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val visibleAreas: Set<AppArea> = emptySet(),
    val showConfiguration: Boolean = false,
    val error: String? = null
)

class DashboardViewModel(
    private val foodRepository: FoodRepository,
    private val authRepository: AuthRepository,
    private val preferences: DashboardPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState(visibleAreas = preferences.getVisibleAreas()))
    val uiState = _uiState.asStateFlow()

    init { observeHistory() }

    fun showConfiguration(show: Boolean) = _uiState.update { it.copy(showConfiguration = show) }

    fun setAreaVisible(area: AppArea, visible: Boolean) {
        val updated = _uiState.value.visibleAreas.toMutableSet().apply {
            if (visible) add(area) else remove(area)
            if (isEmpty()) add(AppArea.NUTRITION)
        }
        preferences.setVisibleAreas(updated)
        _uiState.update { it.copy(visibleAreas = updated) }
    }

    private fun observeHistory() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            foodRepository.observeConsumptionHistory(uid)
                .catch { error -> _uiState.update { it.copy(error = error.message ?: "No se pudo cargar el panel") } }
                .collect { history -> _uiState.update { it.copy(history = history, error = null) } }
        }
    }
}
