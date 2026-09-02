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
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("Resumen de hoy", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("${daily.calories.clean()} kcal · ${daily.proteins.clean()} g proteína", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("${daily.carbs.clean()} g carbohidratos · ${daily.fats.clean()} g grasas", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
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
