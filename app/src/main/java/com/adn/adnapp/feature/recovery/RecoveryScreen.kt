package com.adn.adnapp.feature.recovery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adn.adnapp.core.ui.CompactTopBar
import com.adn.adnapp.core.ui.LocalFloatingNavigationInset
import org.koin.androidx.compose.koinViewModel

@Composable
fun RecoveryScreen(viewModel: RecoveryViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(topBar = { CompactTopBar("Pasos y sueño") }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Recuperación", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Una pulsera puede alimentar estos datos a través de Health Connect cuando concedas permiso.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button({ viewModel.openHealthConnect() }, Modifier.fillMaxWidth().semantics { contentDescription = "Conectar Health Connect" }) { Text("Conectar dispositivo o app") }
            Text(state.connectionMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(state.date, viewModel::onDateChanged, label = { Text("Fecha (AAAA-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Text("Pasos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text("${state.steps} / 8.000") }
                    Text(state.stepSource?.let { "Origen: ${it.label}" } ?: "Sin datos para este día", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(state.stepsInput, viewModel::onStepsChanged, label = { Text("Añadir pasos manualmente") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Button({ viewModel.saveSteps() }, Modifier.fillMaxWidth()) { Text("Guardar pasos") }
                }
            }
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Text("Sueño", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text(if (state.sleepMinutes > 0) "${state.sleepMinutes / 60} h ${state.sleepMinutes % 60} min" else "—") }
                    Text(state.sleepSource?.let { "Origen: ${it.label}" } ?: "Sin datos para este día", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(state.sleepInput, viewModel::onSleepChanged, label = { Text("Horas dormidas") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                    Button({ viewModel.saveSleep() }, Modifier.fillMaxWidth()) { Text("Guardar sueño") }
                }
            }
            Card(shape = RoundedCornerShape(18.dp)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { androidx.compose.material3.Icon(Icons.Default.Info, null); Text("La conexión no duplica el historial manual: cada registro conserva su origen y fecha.", Modifier.padding(start = 10.dp), style = MaterialTheme.typography.bodySmall) } }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) }
        }
    }
}
