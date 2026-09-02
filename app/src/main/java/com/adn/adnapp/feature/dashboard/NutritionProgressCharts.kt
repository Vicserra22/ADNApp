package com.adn.adnapp.feature.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adn.adnapp.core.theme.AdnColors
import com.adn.adnapp.data.model.entity.DailyConsumption
import java.time.LocalDate
import kotlin.math.floor

/**
 * Weekly/monthly progress card. [scoreOf] must return a normalised score from 0 to 1,
 * allowing the caller to plug the user's personalised GoalCalculator result into the chart.
 */
@Composable
fun NutritionProgressCharts(
    history: Map<String, DailyConsumption>,
    scoreOf: (DailyConsumption) -> Double,
    modifier: Modifier = Modifier,
    endDate: LocalDate = LocalDate.now()
) {
    var period by remember { mutableStateOf(ProgressPeriod.WEEK) }
    val model = remember(history, period, endDate, scoreOf) {
        when (period) {
            ProgressPeriod.WEEK -> ProgressChartData.weekly(history, endDate, scoreOf = scoreOf)
            ProgressPeriod.MONTH -> ProgressChartData.monthly(history, endDate, scoreOf = scoreOf)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tu progreso", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "Cumplimiento de objetivos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                PeriodSelector(selected = period, onSelected = { period = it })
            }

            ProgressSummary(model)
            AnimatedContent(targetState = model, label = "progress-period") { current ->
                if (current.trackedDays == 0) EmptyProgressChart()
                else ProgressBarChart(current)
            }
        }
    }
}

@Composable
private fun PeriodSelector(selected: ProgressPeriod, onSelected: (ProgressPeriod) -> Unit) {
    Row(
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
            .padding(3.dp)
    ) {
        PeriodButton("7D", selected == ProgressPeriod.WEEK) { onSelected(ProgressPeriod.WEEK) }
        PeriodButton("30D", selected == ProgressPeriod.MONTH) { onSelected(ProgressPeriod.MONTH) }
    }
}

@Composable
private fun PeriodButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(11.dp)
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ProgressSummary(model: ProgressChartModel) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SummaryMetric("Media", "${model.average.toInt()}%", Modifier.weight(1f))
        SummaryMetric("Mejor", "${model.best.toInt()}%", Modifier.weight(1f))
        val trendText = when {
            model.trend > 0.5f -> "+${model.trend.toInt()}%"
            model.trend < -0.5f -> "${model.trend.toInt()}%"
            else -> "Estable"
        }
        SummaryMetric("Tendencia", trendText, Modifier.weight(1f))
    }
}

@Composable
private fun SummaryMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier.background(AdnColors.Green100, RoundedCornerShape(16.dp)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = AdnColors.Green20)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
            color = AdnColors.Green20)
    }
}

@Composable
private fun ProgressBarChart(model: ProgressChartModel) {
    var selectedIndex by remember(model.period) { mutableStateOf<Int?>(null) }
    val animatedProgress by animateFloatAsState(1f, label = "chart-bars")
    val guideColor = MaterialTheme.colorScheme.outlineVariant
    val barColor = MaterialTheme.colorScheme.primary
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant
    val selectedColor = AdnColors.Green20

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        selectedIndex?.let { index ->
            val point = model.points[index]
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(8.dp).height(8.dp).background(barColor, CircleShape))
                Spacer(Modifier.width(7.dp))
                Text(
                    if (point.hasData) "${point.label}: ${point.value.toInt()}% de cumplimiento"
                    else "${point.label}: sin datos",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        Canvas(
            Modifier.fillMaxWidth().height(180.dp).pointerInput(model.points) {
                detectTapGestures { tap ->
                    val slotWidth = size.width / model.points.size
                    selectedIndex = floor(tap.x / slotWidth).toInt().coerceIn(model.points.indices)
                }
            }
        ) {
            val chartTop = 10.dp.toPx()
            val chartBottom = size.height - 26.dp.toPx()
            val chartHeight = chartBottom - chartTop
            listOf(0f, .5f, 1f).forEach { fraction ->
                val y = chartBottom - chartHeight * fraction
                drawLine(
                    guideColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 5.dp.toPx()))
                )
            }
            val slotWidth = size.width / model.points.size
            val barWidth = slotWidth * .52f
            model.points.forEachIndexed { index, point ->
                val normalised = (point.value / 100f).coerceIn(0f, 1f) * animatedProgress
                val height = if (point.hasData) (chartHeight * normalised).coerceAtLeast(4.dp.toPx())
                    else 3.dp.toPx()
                val left = slotWidth * index + (slotWidth - barWidth) / 2
                drawRoundRect(
                    color = when {
                        selectedIndex == index -> selectedColor
                        point.hasData -> barColor
                        else -> emptyColor
                    },
                    topLeft = Offset(left, chartBottom - height),
                    size = Size(barWidth, height),
                    cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                )
            }
        }
        Row(Modifier.fillMaxWidth()) {
            model.points.forEach { point ->
                Text(
                    point.label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun EmptyProgressChart() {
    Column(
        Modifier.fillMaxWidth().height(180.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Aún no hay suficiente historial", fontWeight = FontWeight.SemiBold)
        Text(
            "Registra tu día para empezar a ver la evolución.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
