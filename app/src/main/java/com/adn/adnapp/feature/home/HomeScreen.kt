package com.adn.adnapp.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import com.adn.adnapp.core.ui.CompactTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(topBar = { CompactTopBar("Inicio") }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Añadir datos de hoy", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Registra cantidades manuales. Cada guardado queda como una entrada independiente y se puede corregir desde el calendario.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            state.dailyConsumption?.let { daily ->
                TodayProgressCard(daily, state.targets)
            }
            Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ManualField(state.manualName, viewModel::onManualNameChanged, "Nombre o nota", KeyboardType.Text)
                    ManualField(state.manualCalories, viewModel::onManualCaloriesChanged, "Calorías (kcal)")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ManualField(state.manualProteins, viewModel::onManualProteinsChanged, "Proteínas (g)", modifier = Modifier.weight(1f))
                        ManualField(state.manualCarbs, viewModel::onManualCarbsChanged, "Carbos (g)", modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ManualField(state.manualFats, viewModel::onManualFatsChanged, "Grasas (g)", modifier = Modifier.weight(1f))
                        ManualField(state.manualSugar, viewModel::onManualSugarChanged, "Azúcar (g)", modifier = Modifier.weight(1f))
                    }
                    Button(
                        onClick = viewModel::saveManualEntry, enabled = !state.isSavingManual,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        if (state.isSavingManual) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                        else Text("Añadir al día de hoy")
                    }
                }
            }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            state.successMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TodayProgressCard(
    daily: com.adn.adnapp.data.model.entity.DailyConsumption,
    targets: com.adn.adnapp.domain.model.NutritionTargets?
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Text(
                "Resumen de hoy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (targets == null) {
                LinearProgressIndicator(Modifier.fillMaxWidth().height(5.dp))
                Text("Cargando los objetivos de tu dieta…", style = MaterialTheme.typography.bodySmall)
            } else {
                MacroProgress("Calorías", daily.calories, targets.calories, "kcal")
                MacroProgress("Proteínas", daily.proteins, targets.proteins, "g")
                MacroProgress("Carbohidratos", daily.carbs, targets.carbs, "g")
                MacroProgress("Grasas", daily.fats, targets.fats, "g")
            }
        }
    }
}

@Composable
private fun MacroProgress(label: String, actual: Double, target: Double, unit: String) {
    val validTarget = target.coerceAtLeast(0.0)
    val ratio = if (validTarget > 0) actual / validTarget else 0.0
    val exceeded = validTarget > 0 && actual > validTarget
    val color = macroProgressColor(ratio, exceeded)
    val difference = kotlin.math.abs(validTarget - actual)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontWeight = FontWeight.SemiBold)
            Text("${actual.clean()} / ${validTarget.clean()} $unit", style = MaterialTheme.typography.bodySmall)
        }
        LinearProgressIndicator(
            progress = { ratio.toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(99.dp)),
            color = color,
            trackColor = color.copy(alpha = .16f)
        )
        Text(
            when {
                exceeded -> "Exceso: ${difference.clean()} $unit"
                difference < .05 -> "Objetivo alcanzado"
                else -> "Te faltan ${difference.clean()} $unit"
            },
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = if (exceeded) FontWeight.Bold else FontWeight.Medium
        )
    }
}

private fun macroProgressColor(ratio: Double, exceeded: Boolean): Color {
    if (exceeded) return Color(0xFF741D35)
    val red = Color(0xFFD64545)
    val orange = Color(0xFFF28C38)
    val yellow = Color(0xFFE2B93B)
    val green = Color(0xFF398F60)
    val value = ratio.coerceIn(0.0, 1.0).toFloat()
    return when {
        value < .33f -> lerp(red, orange, value / .33f)
        value < .66f -> lerp(orange, yellow, (value - .33f) / .33f)
        else -> lerp(yellow, green, (value - .66f) / .34f)
    }
}

@Composable
private fun ManualField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Decimal,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType), modifier = modifier
    )
}

private fun Double.clean() = if (this % 1.0 == 0.0) toInt().toString() else String.format("%.1f", this)
