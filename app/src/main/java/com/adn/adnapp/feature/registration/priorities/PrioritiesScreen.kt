package com.adn.adnapp.feature.registration.priorities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adn.adnapp.domain.model.BodyGoal
import com.adn.adnapp.domain.model.Importance
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrioritiesScreen(
    viewModel: PrioritiesViewModel = koinViewModel(),
    onCompleted: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.completed.collect { onCompleted() } }
    Scaffold(topBar = { TopAppBar(title = { Text("Tus objetivos") }) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState())
                .padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text("¿Qué quieres conseguir?", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BodyGoal.entries.forEach { goal ->
                    FilterChip(
                        selected = state.bodyGoal == goal, onClick = { viewModel.setBodyGoal(goal) },
                        label = { Text(goal.label()) }, modifier = Modifier.weight(1f)
                    )
                }
            }
            OutlinedTextField(
                value = state.targetWeight, onValueChange = viewModel::setTargetWeight,
                label = { Text("Peso objetivo (kg)") }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true
            )
            Text("¿Cuánto pesa cada área en tu progreso?", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
            ImportanceSlider("Nutrición", state.nutrition, viewModel::setNutrition)
            ImportanceSlider("Deporte", state.sports, viewModel::setSports)
            ImportanceSlider("Objetivos personales", state.goals, viewModel::setGoals)
            Text("La valoración diaria y los colores del calendario usarán estas prioridades.",
                style = MaterialTheme.typography.bodySmall)
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = viewModel::save, modifier = Modifier.fillMaxWidth(), enabled = !state.isSaving) {
                if (state.isSaving) CircularProgressIndicator() else Text("Guardar prioridades")
            }
        }
    }
}

@Composable
private fun ImportanceSlider(label: String, value: Importance, onChange: (Importance) -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontWeight = FontWeight.Medium)
            Text(value.label(), color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value.weight.toFloat(), onValueChange = { raw ->
                onChange(Importance.entries[(raw.toInt() - 1).coerceIn(0, 4)])
            }, valueRange = 1f..5f, steps = 3
        )
    }
}

private fun BodyGoal.label() = when (this) {
    BodyGoal.LOSE_WEIGHT -> "Adelgazar"
    BodyGoal.MAINTAIN -> "Mantener"
    BodyGoal.GAIN_WEIGHT -> "Ganar"
}

private fun Importance.label() = when (this) {
    Importance.VERY_LOW -> "Muy poco"
    Importance.LOW -> "Poco"
    Importance.NORMAL -> "Normal"
    Importance.IMPORTANT -> "Importante"
    Importance.VERY_IMPORTANT -> "Muy importante"
}
