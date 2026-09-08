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
import com.adn.adnapp.domain.model.DailyActivityLevel
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
    val targetsByActivity: Map<DailyActivityLevel, NutritionTargets> = emptyMap(),
    val profile: UserProfile? = null,
    val isSavingActivity: Boolean = false,
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

    fun setTodayActivity(level: DailyActivityLevel) {
        val uid = authRepository.getCurrentUserId() ?: return
        val date = _uiState.value.selectedDate
        val current = _uiState.value.history[date] ?: DailyConsumption(date = date)
        val updatedHistory = _uiState.value.history + (date to current.copy(activityLevel = level))
        _uiState.update {
            it.copy(
                history = updatedHistory,
                targets = it.targetsByActivity[level] ?: it.targets,
                isSavingActivity = true,
                error = null
            )
        }
        viewModelScope.launch {
            foodRepository.setDailyActivityLevel(uid, date, level).fold(
                onSuccess = { _uiState.update { it.copy(isSavingActivity = false) } },
                onFailure = {
                    _uiState.update { it.copy(isSavingActivity = false, error = "No se pudo guardar la actividad del día") }
                }
            )
        }
    }

    private fun loadGoalsAndHistory() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val profile = userRepository.getUserProfile(uid).getOrNull()
            val nutrition = nutritionRepository.getNutritionProfile(uid).getOrNull()
            val diet = nutrition?.dietId?.let { dietRepository.getDiet(it, uid).getOrNull() }
            val targetsByActivity = if (profile != null && diet != null) {
                DailyActivityLevel.entries.associateWith {
                    GoalCalculator.targets(profile, diet, it, nutrition.macroTolerance)
                }
            } else emptyMap()
            val defaultTargets = targetsByActivity[DailyActivityLevel.LIGHT]
            _uiState.update {
                it.copy(profile = profile, targets = defaultTargets, targetsByActivity = targetsByActivity)
            }
            foodRepository.observeConsumptionHistory(uid)
                .catch { error -> _uiState.update { it.copy(error = error.message ?: "No se pudo cargar el panel") } }
                .collect { history ->
                    val scores = if (profile != null && defaultTargets != null) {
                        history.filterValues(DailyConsumption::hasTrackedNutrition).mapValues { (_, daily) ->
                            GoalCalculator.score(
                                daily,
                                targetsByActivity[daily.activityLevel] ?: defaultTargets,
                                profile
                            )
                        }
                    } else emptyMap()
                    val selectedLevel = history[_uiState.value.selectedDate]?.activityLevel ?: DailyActivityLevel.LIGHT
                    _uiState.update {
                        it.copy(
                            history = history,
                            dayScores = scores,
                            targets = targetsByActivity[selectedLevel] ?: defaultTargets,
                            error = null
                        )
                    }
                }
        }
    }
}

private fun DailyConsumption.hasTrackedNutrition() = calories > 0 || proteins > 0 || carbs > 0 ||
    fats > 0 || sugar > 0 || waterMl > 0
