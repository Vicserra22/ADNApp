package com.adn.adnapp.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.adn.adnapp.core.ui.CompactTopBar
import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.domain.model.GoalRule
import com.adn.adnapp.domain.model.NutrientGoal
import com.adn.adnapp.domain.model.NutritionTargets
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    Scaffold(topBar = { CompactTopBar("Inicio") }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Inicio", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Este espacio queda libre para el próximo módulo.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ManualNutritionCard(state: HomeUiState, viewModel: HomeViewModel) {
    state.dateChoice?.let {
        com.adn.adnapp.core.ui.EntryDateSheet(it, viewModel::confirmEntryDate, viewModel::cancelDateChoice)
    }
    Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Añadir datos manualmente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Cada guardado crea una entrada independiente que podrás editar desde el calendario.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ManualField(
                state.manualName,
                viewModel::onManualNameChanged,
                "Nombre o nota",
                Modifier.fillMaxWidth(),
                KeyboardType.Text
            )
            ManualField(
                state.manualCalories,
                viewModel::onManualCaloriesChanged,
                "Calorías (kcal)",
                Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ManualField(state.manualProteins, viewModel::onManualProteinsChanged, "Proteínas (g)", modifier = Modifier.weight(1f))
                ManualField(state.manualCarbs, viewModel::onManualCarbsChanged, "Carbos (g)", modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ManualField(state.manualFats, viewModel::onManualFatsChanged, "Grasas (g)", modifier = Modifier.weight(1f))
                ManualField(state.manualSugar, viewModel::onManualSugarChanged, "Azúcar (g)", modifier = Modifier.weight(1f))
            }
            Button(
                onClick = viewModel::saveManualEntry,
                enabled = !state.isSavingManual,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (state.isSavingManual) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                else Text("Añadir al " + state.screenDate)
            }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            state.successMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}


@Composable
private fun ManualField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Decimal
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier
    )
}

private fun Double.clean() = if (this % 1.0 == 0.0) toInt().toString()
else String.format(java.util.Locale.getDefault(), "%.1f", this)
