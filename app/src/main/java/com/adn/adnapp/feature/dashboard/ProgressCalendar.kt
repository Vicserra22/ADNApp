package com.adn.adnapp.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adn.adnapp.domain.model.DayScore
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ProgressCalendar(
    scores: Map<String, DayScore>,
    onDaySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var monthKey by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    val month = YearMonth.parse(monthKey)
    val today = LocalDate.now()
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp).pointerInput(monthKey) {
            var distance = 0f
            detectHorizontalDragGestures(
                onDragStart = { distance = 0f },
                onHorizontalDrag = { change, delta -> change.consume(); distance += delta },
                onDragEnd = {
                    val threshold = 56.dp.toPx()
                    if (distance > threshold) monthKey = month.minusMonths(1).toString()
                    else if (distance < -threshold && month < YearMonth.now())
                        monthKey = month.plusMonths(1).toString()
                }
            )
        }, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = { monthKey = month.minusMonths(1).toString() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Mes anterior")
                }
                Text(
                    month.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
                        .replaceFirstChar { it.titlecase() } + " ${month.year}",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { monthKey = month.plusMonths(1).toString() }, enabled = month < YearMonth.now()) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Mes siguiente")
                }
            }
            Row(Modifier.fillMaxWidth()) {
                listOf("L", "M", "X", "J", "V", "S", "D").forEach {
                    Text(it, Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium)
                }
            }
            val first = month.atDay(1)
            val leading = first.dayOfWeek.value - DayOfWeek.MONDAY.value
            val cells = (leading + month.lengthOfMonth() + 6) / 7 * 7
            repeat(cells / 7) { row ->
                Row(Modifier.fillMaxWidth()) {
                    repeat(7) { column ->
                        val dayNumber = row * 7 + column - leading + 1
                        val date = if (dayNumber in 1..month.lengthOfMonth()) month.atDay(dayNumber) else null
                        val score = date?.let { scores[it.toString()] }
                        CalendarCell(
                            day = date, score = score, enabled = date != null && !date.isAfter(today),
                            onClick = { date?.let { onDaySelected(it.toString()) } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            CalendarLegend()
        }
    }
}

@Composable
private fun CalendarCell(
    day: LocalDate?, score: DayScore?, enabled: Boolean, onClick: () -> Unit, modifier: Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(modifier.aspectRatio(1f).padding(3.dp), contentAlignment = Alignment.Center) {
        if (day != null) {
            val color = score?.let { progressColor(it.total) }
                ?: MaterialTheme.colorScheme.surfaceVariant
            Box(
                Modifier.size(36.dp).background(color.copy(alpha = if (enabled) 1f else .35f), CircleShape)
                    .clickable(
                        enabled = enabled,
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(day.dayOfMonth.toString(), color = if (score != null) Color.White
                    else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun CalendarLegend() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        listOf(0.2 to "Lejos", 0.5 to "Mejorable", 0.75 to "Cerca", 0.95 to "Objetivo").forEach { (v, label) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(9.dp).background(progressColor(v), CircleShape))
                Text(" $label", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

fun progressColor(score: Double): Color {
    val red = Color(0xFFD64545)
    val orange = Color(0xFFF28C38)
    val yellow = Color(0xFFE2B93B)
    val green = Color(0xFF398F60)
    val value = score.coerceIn(0.0, 1.0).toFloat()
    return when {
        value < .33f -> lerp(red, orange, value / .33f)
        value < .66f -> lerp(orange, yellow, (value - .33f) / .33f)
        else -> lerp(yellow, green, (value - .66f) / .34f)
    }
}
