package com.adn.adnapp.feature.dayviewer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayViewerScreen(date: String, onBack: () -> Unit) {
    val viewModel: DayViewerViewModel = koinViewModel(parameters = { parametersOf(date) })
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(formatDate(date)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (state.isEditing) "Editar macros del día" else "Resumen del día",
                style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold
            )
            Text(
                if (state.isEditing) "Los valores guardados sustituirán el total de este día."
                else "Este visor no modifica nada hasta que pulses Editar.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                state.isEditing -> MacroEditor(state, viewModel)
                else -> MacroSummary(state)
            }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            state.savedMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.height(4.dp))
            if (!state.isLoading) {
                if (state.isEditing) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = viewModel::cancelEditing, Modifier.weight(1f),
                            enabled = !state.isSaving) { Text("Cancelar") }
                        Button(onClick = viewModel::saveManualMacros, Modifier.weight(1f),
                            enabled = !state.isSaving) {
                            if (state.isSaving) CircularProgressIndicator(Modifier.height(20.dp)) else Text("Guardar")
                        }
                    }
                } else {
                    Button(onClick = viewModel::startEditing, Modifier.fillMaxWidth()) {
                        Text("Editar o añadir macros")
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroSummary(state: DayViewerUiState) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MacroRow("Calorías", "${state.consumption.calories.clean()} kcal")
            MacroRow("Proteínas", "${state.consumption.proteins.clean()} g")
            MacroRow("Carbohidratos", "${state.consumption.carbs.clean()} g")
            MacroRow("Grasas", "${state.consumption.fats.clean()} g")
        }
    }
}

@Composable
private fun MacroRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MacroEditor(state: DayViewerUiState, viewModel: DayViewerViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        MacroField("Calorías (kcal)", state.calories, viewModel::onCaloriesChanged)
        MacroField("Proteínas (g)", state.proteins, viewModel::onProteinsChanged)
        MacroField("Carbohidratos (g)", state.carbs, viewModel::onCarbsChanged)
        MacroField("Grasas (g)", state.fats, viewModel::onFatsChanged)
    }
}

@Composable
private fun MacroField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        modifier = Modifier.fillMaxWidth(), singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}

private fun formatDate(value: String): String = runCatching {
    LocalDate.parse(value).format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "ES")))
        .replaceFirstChar { it.titlecase(Locale("es", "ES")) }
}.getOrDefault(value)

private fun Double.clean() = if (this % 1.0 == 0.0) toInt().toString() else "%.1f".format(this)
