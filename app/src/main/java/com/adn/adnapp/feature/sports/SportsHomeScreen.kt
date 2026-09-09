package com.adn.adnapp.feature.sports

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.adn.adnapp.core.ui.LocalFloatingNavigationInset
import com.adn.adnapp.domain.model.SportKind
import com.adn.adnapp.domain.model.SportSession
import org.koin.androidx.compose.koinViewModel

@Composable
fun SportsHomeScreen(onOpenRecovery: () -> Unit = {}, viewModel: SportsViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 14.dp, 16.dp, LocalFloatingNavigationInset.current + 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Text("Tu movimiento", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Registra lo que haces y compara sesiones del mismo deporte.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item { SportCarousel(state.selectedSport, viewModel::selectSport) }
            item { SportsSummary(state) }
            item { Button(onOpenRecovery, Modifier.fillMaxWidth().semantics { contentDescription = "Abrir Pasos y sueño" }) { Text("Pasos y sueño") } }
            item { SessionForm(state, viewModel) }
            if (state.selectedSessions.isEmpty()) item { Text("Aún no hay sesiones de ${state.selectedSport.label.lowercase()}.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(state.selectedSessions, key = { it.id }) { session -> SessionCard(session, viewModel::remove) }
            state.error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
            state.message?.let { item { Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) } }
        }
    }
}

@Composable
private fun SportCarousel(selected: SportKind, onSelect: (SportKind) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SportKind.entries.forEach { sport ->
            FilterChip(
                selected == sport,
                { onSelect(sport) },
                leadingIcon = { SportSilhouette(sport) },
                label = { Text(sport.label) },
                modifier = Modifier.semantics { contentDescription = "Deporte ${sport.label}" }
            )
        }
    }
}

@Composable
private fun SportSilhouette(sport: SportKind) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(Modifier.size(26.dp)) {
        val cx = size.width / 2f
        val head = size.minDimension * .14f
        drawCircle(color, head, androidx.compose.ui.geometry.Offset(cx, size.height * .2f))
        val path = androidx.compose.ui.graphics.Path().apply {
            when (sport) {
                SportKind.STRENGTH -> { moveTo(cx, size.height*.32f); lineTo(cx, size.height*.67f); moveTo(cx-size.width*.28f,size.height*.42f); lineTo(cx+size.width*.28f,size.height*.42f); moveTo(cx,size.height*.67f); lineTo(cx-size.width*.22f,size.height*.95f); moveTo(cx,size.height*.67f); lineTo(cx+size.width*.22f,size.height*.95f) }
                SportKind.YOGA -> { moveTo(cx,size.height*.32f); cubicTo(cx-size.width*.35f,size.height*.42f,cx-size.width*.35f,size.height*.68f,cx,size.height*.68f); lineTo(cx+size.width*.28f,size.height*.92f) }
                SportKind.SWIMMING -> { moveTo(size.width*.08f,size.height*.7f); cubicTo(size.width*.25f,size.height*.5f,size.width*.45f,size.height*.85f,size.width*.62f,size.height*.63f); cubicTo(size.width*.75f,size.height*.45f,size.width*.87f,size.height*.62f,size.width*.95f,size.height*.5f) }
                SportKind.CYCLING -> { moveTo(size.width*.2f,size.height*.75f); lineTo(cx,size.height*.38f); lineTo(size.width*.8f,size.height*.75f); lineTo(size.width*.2f,size.height*.75f); moveTo(cx,size.height*.38f); lineTo(cx+size.width*.2f,size.height*.22f) }
                else -> { moveTo(cx,size.height*.32f); lineTo(cx,size.height*.72f); moveTo(cx,size.height*.4f); lineTo(cx-size.width*.25f,size.height*.62f); moveTo(cx,size.height*.4f); lineTo(cx+size.width*.25f,size.height*.55f); moveTo(cx,size.height*.72f); lineTo(cx-size.width*.2f,size.height*.95f); moveTo(cx,size.height*.72f); lineTo(cx+size.width*.25f,size.height*.95f) }
            }
        }
        drawPath(path, color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.4f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    }
}

@Composable
private fun SportsSummary(state: SportsUiState) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Stat("Sesiones", state.selectedSessions.size.toString())
            Stat("7 días", "${state.lastSevenMinutes} min")
            Stat("Mejor", if (state.bestDistance > 0) "${"%.1f".format(state.bestDistance)} km" else "—")
        }
    }
}

@Composable private fun Stat(label: String, value: String) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text(label, style = MaterialTheme.typography.labelSmall) } }

@Composable
private fun SessionForm(state: SportsUiState, viewModel: SportsViewModel) {
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("Registrar sesión", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(state.selectedDate, viewModel::onDateChanged, label = { Text("Fecha (AAAA-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(state.minutesInput, viewModel::onMinutesChanged, label = { Text("Duración (minutos)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            if (state.selectedSport.family == "cardio") {
                OutlinedTextField(state.distanceInput, viewModel::onDistanceChanged, label = { Text("Distancia (km), opcional") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
            }
            if (state.selectedSport == SportKind.STRENGTH) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(state.setsInput, viewModel::onSetsChanged, label = { Text("Series") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(state.repsInput, viewModel::onRepsChanged, label = { Text("Repeticiones") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(state.weightInput, viewModel::onWeightChanged, label = { Text("Kg") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
                }
            }
            OutlinedTextField(state.noteInput, viewModel::onNoteChanged, label = { Text("Nota o recorrido") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Button({ viewModel.addSession() }, Modifier.fillMaxWidth().semantics { contentDescription = "Guardar sesión" }) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Guardar sesión") }
        }
    }
}

@Composable
private fun SessionCard(session: SportSession, onRemove: (SportSession) -> Unit) {
    Card(shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Text("${session.date} · ${session.minutes} min", fontWeight = FontWeight.Bold)
                val details = buildList { if (session.distanceKm > 0) add("${"%.1f".format(session.distanceKm)} km"); if (session.sets > 0) add("${session.sets}×${session.reps} · ${session.weightKg} kg") }.joinToString(" · ")
                if (details.isNotBlank()) Text(details, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (session.note.isNotBlank()) Text(session.note, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton({ onRemove(session) }, Modifier.semantics { contentDescription = "Eliminar sesión ${session.date}" }) { Icon(Icons.Default.Delete, "Eliminar") }
        }
    }
}

@Composable
fun SportsAnalysisScreen(viewModel: SportsViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Tendencias de ${state.selectedSport.label.lowercase()}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            SportCarousel(state.selectedSport, viewModel::selectSport)
            SportsSummary(state)
            val message = when {
                state.selectedSessions.size < 2 -> "Registra al menos dos sesiones para comparar una tendencia."
                state.lastSevenMinutes == 0 -> "No hay minutos en los últimos 7 días."
                else -> "Has acumulado ${state.lastSevenMinutes} minutos en los últimos 7 días y tu mejor distancia es ${"%.1f".format(state.bestDistance)} km."
            }
            Card(shape = RoundedCornerShape(20.dp)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Info, null); Text(message, Modifier.padding(start = 10.dp)) } }
            Text("Los datos se comparan dentro del mismo deporte; cambiar de variante o recorrido puede alterar la métrica.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
