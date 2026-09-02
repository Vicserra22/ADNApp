package com.adn.adnapp.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adn.adnapp.data.local.DashboardPreferences
import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.domain.model.AppArea
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.FoodRepository
import com.adn.adnapp.domain.repository.UserRepository
import com.adn.adnapp.domain.repository.NutritionRepository
import com.adn.adnapp.domain.repository.DietRepository
import com.adn.adnapp.domain.model.DayScore
import com.adn.adnapp.domain.model.NutritionTargets
import com.adn.adnapp.data.model.entity.UserProfile
import com.adn.adnapp.domain.service.GoalCalculator
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
    val dayScores: Map<String, DayScore> = emptyMap(),
    val targets: NutritionTargets? = null,
    val profile: UserProfile? = null,
    val showConfiguration: Boolean = false,
    val error: String? = null
)

class DashboardViewModel(
    private val foodRepository: FoodRepository,
    private val authRepository: AuthRepository,
    private val preferences: DashboardPreferences,
    private val userRepository: UserRepository,
    private val nutritionRepository: NutritionRepository,
    private val dietRepository: DietRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState(visibleAreas = preferences.getVisibleAreas()))
    val uiState = _uiState.asStateFlow()

    init { loadGoalsAndHistory() }

    fun showConfiguration(show: Boolean) = _uiState.update { it.copy(showConfiguration = show) }

    fun setAreaVisible(area: AppArea, visible: Boolean) {
        val updated = _uiState.value.visibleAreas.toMutableSet().apply {
            if (visible) add(area) else remove(area)
            if (isEmpty()) add(AppArea.NUTRITION)
        }
        preferences.setVisibleAreas(updated)
        _uiState.update { it.copy(visibleAreas = updated) }
    }

    private fun loadGoalsAndHistory() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val profile = userRepository.getUserProfile(uid).getOrNull()
            val nutrition = nutritionRepository.getNutritionProfile(uid).getOrNull()
            val diet = nutrition?.dietId?.let { dietRepository.getDiet(it).getOrNull() }
            val targets = if (profile != null && diet != null) GoalCalculator.targets(profile, diet) else null
            _uiState.update { it.copy(profile = profile, targets = targets) }
            foodRepository.observeConsumptionHistory(uid)
                .catch { error -> _uiState.update { it.copy(error = error.message ?: "No se pudo cargar el panel") } }
                .collect { history ->
                    val scores = if (profile != null && targets != null) {
                        history.mapValues { (_, daily) -> GoalCalculator.score(daily, targets, profile) }
                    } else emptyMap()
                    _uiState.update { it.copy(history = history, dayScores = scores, error = null) }
                }
        }
    }
}
