package com.adn.adnapp.feature.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adn.adnapp.domain.model.AppArea
import com.adn.adnapp.domain.model.DailyActivityLevel
import com.adn.adnapp.core.ui.CompactTopBar
import com.adn.adnapp.domain.service.GoalCalculator
import com.adn.adnapp.feature.home.HomeViewModel
import com.adn.adnapp.feature.home.ManualNutritionCard
import com.adn.adnapp.core.ui.NutritionProgressCard
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel(),
    nutritionViewModel: HomeViewModel = koinViewModel(),
    onOpenDay: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val nutritionState by nutritionViewModel.uiState.collectAsStateWithLifecycle()
    val todayConsumption = state.history[state.selectedDate]

    Scaffold(topBar = { CompactTopBar("Nutrición") }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Nutrición · " + nutritionState.screenDate,
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Indica la actividad del día para ajustar la estimación energética sin cambiar tu dieta guardada.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                val selected = todayConsumption?.activityLevel ?: DailyActivityLevel.LIGHT
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DailyActivityLevel.entries.forEach { level ->
                        FilterChip(
                            selected = selected == level,
                            onClick = { viewModel.setTodayActivity(level) },
                            enabled = !state.isSavingActivity,
                            label = { Text(level.shortLabel()) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            item {
                NutritionProgressCard(
                    daily = todayConsumption ?: nutritionState.dailyConsumption
                        ?: com.adn.adnapp.data.model.entity.DailyConsumption(state.selectedDate),
                    targets = state.targets
                )
            }
            item { ManualNutritionCard(nutritionState, nutritionViewModel) }
            item {
                Row(
                    Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Calendario nutricional", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
            item {
                ProgressCalendar(scores = state.dayScores, onDaySelected = onOpenDay)
                Text(
                    "Gris significa que no hay datos. Los demás tonos reflejan tu cumplimiento ponderado.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            if (state.profile != null && state.targets != null) {
                item {
                    NutritionProgressCharts(
                        history = state.history,
                        scoreOf = { daily ->
                            val targets = state.targetsByActivity[daily.activityLevel] ?: state.targets!!
                            GoalCalculator.score(daily, targets, state.profile!!).total
                        }
                    )
                }
            }
            items(listOf(AppArea.NUTRITION)) { area ->
                val clickable = if (area == AppArea.NUTRITION) {
                    Modifier.clickable { onOpenDay(state.selectedDate) }
                } else Modifier
                Card(Modifier.fillMaxWidth().then(clickable)) {
                    Column(Modifier.padding(18.dp)) {
                        Text(area.label(), fontWeight = FontWeight.Bold)
                        if (area == AppArea.NUTRITION) {
                            Text(
                                if (todayConsumption == null) "Sin datos hoy · Toca para ver el día"
                                else "${todayConsumption.calories.toInt()} kcal · " +
                                    "${todayConsumption.proteins.toInt()} g proteína · Toca para ver"
                            )
                        } else Text("Próximamente", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            state.error?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.error) } }
        }
    }

}

private fun DailyActivityLevel.shortLabel() = when (this) {
    DailyActivityLevel.SEDENTARY -> "Reposo"
    DailyActivityLevel.LIGHT -> "Normal"
    DailyActivityLevel.ACTIVE -> "Entreno"
    DailyActivityLevel.VERY_ACTIVE -> "Intenso"
}

private fun AppArea.label() = when (this) {
    AppArea.AGENDA -> "Agenda"
    AppArea.NUTRITION -> "Nutrición"
    AppArea.SPORTS -> "Deportes"
    AppArea.FINANCE -> "Finanzas"
    AppArea.PHILOSOPHY -> "Filosofía"
}
