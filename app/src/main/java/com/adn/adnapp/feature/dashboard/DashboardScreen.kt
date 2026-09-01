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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adn.adnapp.domain.model.AppArea
import java.time.Instant
import java.time.ZoneOffset
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel(),
    onOpenDay: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val todayConsumption = state.history[state.selectedDate]
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Dashboard") }) }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tus áreas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Button(onClick = { viewModel.showConfiguration(true) }) { Text("Personalizar") }
                }
            }
            item {
                Button(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Abrir calendario")
                }
                Text(
                    "El calendario abre un visor. Para cambiar datos tendrás que pulsar Editar dentro del día.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            items(state.visibleAreas.sortedBy { it.ordinal }) { area ->
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

    if (showDatePicker) {
        val today = remember { System.currentTimeMillis() }
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = today,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= today
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { onOpenDay(it.toDateKey()) }
                        showDatePicker = false
                    },
                    enabled = pickerState.selectedDateMillis != null
                ) { Text("Ver día") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) { DatePicker(state = pickerState) }
    }

    if (state.showConfiguration) {
        AlertDialog(
            onDismissRequest = { viewModel.showConfiguration(false) },
            title = { Text("Configurar dashboard") },
            text = {
                Column {
                    AppArea.entries.forEach { area ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                state.visibleAreas.contains(area),
                                onCheckedChange = { viewModel.setAreaVisible(area, it) }
                            )
                            Text(area.label())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.showConfiguration(false) }) { Text("Listo") }
            }
        )
    }
}

private fun Long.toDateKey(): String = Instant.ofEpochMilli(this)
    .atZone(ZoneOffset.UTC).toLocalDate().toString()

private fun AppArea.label() = when (this) {
    AppArea.NUTRITION -> "Nutrición"
    AppArea.SPORTS -> "Deportes"
    AppArea.FINANCE -> "Finanzas"
    AppArea.PHILOSOPHY -> "Filosofía"
}
