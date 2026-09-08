package com.adn.adnapp.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.adn.adnapp.domain.model.AppArea

@Composable
fun AreaCycleSettings(cycle: List<AppArea>, onChange: (List<AppArea>) -> Unit, onDismiss: () -> Unit) {
    AppBottomSheet(onDismiss) {
        Text("Cambio rápido de área", style = MaterialTheme.typography.titleLarge)
        Text("Mantén la bola central 1,5 segundos para avanzar una vez. Elige al menos dos áreas y su orden.")
        Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
            val ordered = cycle + AppArea.entries.filterNot { it in cycle }
            ordered.forEach { area ->
                val index = cycle.indexOf(area)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(index >= 0, onCheckedChange = { checked ->
                        if (checked) onChange(cycle + area)
                        else if (cycle.size > 2) onChange(cycle - area)
                    }, enabled = index < 0 || cycle.size > 2)
                    Text(area.displayName, Modifier.weight(1f))
                    if (index >= 0) {
                        TextButton(
                            onClick = { onChange(cycle.toMutableList().apply {
                                this[index] = this[index - 1]; this[index - 1] = area
                            }) }, enabled = index > 0
                        ) { Text("↑") }
                        TextButton(
                            onClick = { onChange(cycle.toMutableList().apply {
                                this[index] = this[index + 1]; this[index + 1] = area
                            }) }, enabled = index < cycle.lastIndex
                        ) { Text("↓") }
                    }
                }
            }
        }
        Text("Los cambios se guardan automáticamente en este dispositivo.",
            style = MaterialTheme.typography.bodySmall)
        Button(onDismiss, Modifier.fillMaxWidth()) { Text("Listo") }
    }
}

val AppArea.displayName get() = when (this) {
    AppArea.AGENDA -> "Agenda"
    AppArea.NUTRITION -> "Nutrición"
    AppArea.SPORTS -> "Deporte"
    AppArea.FINANCE -> "Finanzas"
    AppArea.PHILOSOPHY -> "Filosofía"
}

