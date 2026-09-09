package com.adn.adnapp.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adn.adnapp.core.ui.*
import com.adn.adnapp.domain.model.AppArea
import org.koin.androidx.compose.koinViewModel

@Composable
fun AgendaAnalysisScreen(
    viewModel: DashboardViewModel = koinViewModel(),
    onOpenNutritionDay: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedDay by rememberSaveable { mutableStateOf<String?>(null) }
    // Only implemented modules contribute scores. Missing modules never count as failures.
    val scores = if (AppArea.NUTRITION in state.visibleAreas) state.dayScores else emptyMap()
    Scaffold { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).statusBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 38.dp, bottom = LocalFloatingNavigationInset.current + 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tu calendario", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    TextButton({ viewModel.showConfiguration(true) }, contentPadding = PaddingValues(8.dp)) {
                        Text("Filtrar", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            item { ProgressCalendar(scores, { selectedDay = it }) }
            item { Text("El color resume las áreas seleccionadas con datos. Las áreas pendientes no bajan la valoración.",
                style = MaterialTheme.typography.bodySmall) }
            state.error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
        }
    }
    if (state.showConfiguration) AppBottomSheet({ viewModel.showConfiguration(false) }) {
        Text("Filtrar áreas", style = MaterialTheme.typography.titleLarge)
        AppArea.entries.forEach { area ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(area in state.visibleAreas, { viewModel.setAreaVisible(area, it) })
                Text(area.displayName)
            }
        }
        Button({ viewModel.showConfiguration(false) }, Modifier.fillMaxWidth()) { Text("Aplicar") }
    }
    selectedDay?.let { date ->
        AppBottomSheet({ selectedDay = null }) {
            Text("Tu día · " + date.readableDate(), style = MaterialTheme.typography.titleLarge)
            state.visibleAreas.forEach { area ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(area.displayName, fontWeight = FontWeight.Bold)
                        val score = if (area == AppArea.NUTRITION) state.dayScores[date] else null
                        if (score != null) {
                            Text("Cumplimiento: " + score.percent + "%")
                            val parts = listOf("Calorías" to score.calories, "Proteína" to score.proteins,
                                "Carbohidratos" to score.carbs, "Grasas" to score.fats,
                                "Azúcar" to score.sugar, "Agua" to score.water)
                            val best = parts.maxBy { it.second }
                            val worst = parts.minBy { it.second }
                            Text("Mejor: " + best.first + " · " + (best.second * 100).toInt() + "%")
                            if (worst.second < best.second)
                                Text("A mejorar: " + worst.first + " · " + (worst.second * 100).toInt() + "%")
                            else Text("Todos los indicadores tienen el mismo cumplimiento.")
                        } else Text(if (area == AppArea.NUTRITION) "Sin registros este día"
                            else "Sin datos · módulo pendiente")
                        if (area == AppArea.NUTRITION) TextButton({
                            selectedDay = null
                            onOpenNutritionDay(date)
                        }) { Text("Ver detalle nutricional") }
                    }
                }
            }
        }
    }
}

