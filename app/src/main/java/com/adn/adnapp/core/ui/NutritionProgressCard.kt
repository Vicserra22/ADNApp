package com.adn.adnapp.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.domain.model.GoalRule
import com.adn.adnapp.domain.model.NutrientGoal
import com.adn.adnapp.domain.model.NutritionTargets
import kotlin.math.abs

/** One presentation and tolerance policy for today's summary and historical day details. */
@Composable
fun NutritionProgressCard(daily: DailyConsumption, targets: NutritionTargets?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Text(
                "Resumen · " + daily.date,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (targets == null) {
                LinearProgressIndicator(Modifier.fillMaxWidth().height(5.dp))
                Text("Cargando los objetivos de tu dieta…", style = MaterialTheme.typography.bodySmall)
            } else {
                MacroProgress("Calorías", daily.calories, targets.caloriesGoal, "kcal")
                MacroProgress("Proteínas", daily.proteins, targets.proteinGoal, "g")
                MacroProgress("Carbohidratos", daily.carbs, targets.carbsGoal, "g")
                MacroProgress("Grasas", daily.fats, targets.fatsGoal, "g")
            }
        }
    }
}

@Composable
private fun MacroProgress(
    label: String,
    actual: Double,
    goal: NutrientGoal,
    unit: String
) {
    val target = goal.target.coerceAtLeast(0.0)
    val ratio = if (target > 0) actual / target else 0.0
    val minimum = goal.minimum ?: 0.0
    val acceptedMaximum = goal.maximum
    val below = minimum > 0 && actual < minimum
    val above = acceptedMaximum != null && actual > acceptedMaximum
    val critical = goal.criticalMaximum?.let { actual >= it } == true
    val color = when {
        critical -> Color(0xFF741D35)
        above -> Color(0xFFD47A28)
        below -> progressApproachColor(actual / minimum)
        else -> Color(0xFF398F60)
    }
    val text = when {
        target <= 0 -> "Sin objetivo configurado"
        below -> "Te faltan ${(minimum - actual).clean()} $unit para entrar en rango"
        goal.rule == GoalRule.MINIMUM && actual > target -> "Por encima del objetivo · sin penalización"
        above && critical -> "Exceso alto: ${abs(actual - target).clean()} $unit"
        above -> "Ligeramente por encima del rango"
        else -> "Dentro del rango objetivo"
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontWeight = FontWeight.SemiBold)
            Text("${actual.clean()} / ${target.clean()} $unit", style = MaterialTheme.typography.bodySmall)
        }
        LinearProgressIndicator(
            progress = { ratio.toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(99.dp)),
            color = color,
            trackColor = color.copy(alpha = .16f)
        )
        Text(text, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Medium)
    }
}

private fun progressApproachColor(ratio: Double): Color {
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
private fun Double.clean() = if (this % 1.0 == 0.0) toInt().toString()
else String.format(java.util.Locale.getDefault(), "%.1f", this)

