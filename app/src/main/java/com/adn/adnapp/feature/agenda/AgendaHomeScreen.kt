package com.adn.adnapp.feature.agenda

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adn.adnapp.core.ui.CompactTopBar
import com.adn.adnapp.core.ui.LocalFloatingNavigationInset
import com.adn.adnapp.core.ui.readableDate
import com.adn.adnapp.domain.model.AgendaItem
import com.adn.adnapp.domain.model.AgendaItemKind
import com.adn.adnapp.domain.model.AgendaProject
import java.time.LocalDate
import org.koin.androidx.compose.koinViewModel

@Composable
fun AgendaHomeScreen(viewModel: AgendaViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 38.dp, bottom = LocalFloatingNavigationInset.current + 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("Tu mesa de trabajo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Lo previsto, lo pendiente y el siguiente paso en una sola vista.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item { DateStrip(state.selectedDate, viewModel::chooseDate, viewModel::previousDay, viewModel::nextDay) }
            item { ViewSelector(state.view, state.inboxCount, viewModel::setView) }
            item { QuickCapture(state, viewModel) }
            if (state.view == AgendaView.TODAY || state.view == AgendaView.WEEK) {
                item { TodaySummary(state.items) }
            }
            if (state.items.isEmpty() && state.view != AgendaView.PROJECTS) item {
                EmptyAgenda(state.view)
            }
            items(state.items, key = { it.id }) { item -> AgendaItemCard(item, state.projects, viewModel) }
            if (state.view == AgendaView.PROJECTS || state.projects.isNotEmpty()) {
                item { ProjectComposer(state, viewModel) }
                items(state.projects, key = { it.id }) { project -> ProjectCard(project, state.items, viewModel) }
            }
            state.error?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.error) } }
            state.message?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) } }
        }
    }
}

@Composable
private fun DateStrip(date: String, onDate: (String) -> Unit, previous: () -> Unit, next: () -> Unit) {
    val selected = LocalDate.parse(date)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(date.readableDate(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            TextButton(previous) { Text("‹") }
            TextButton({ onDate(LocalDate.now().toString()) }) { Text("Hoy") }
            TextButton(next) { Text("›") }
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (-2L..4L).forEach { offset ->
                val day = selected.plusDays(offset)
                FilterChip(
                    selected = day == selected,
                    onClick = { onDate(day.toString()) },
                    label = { Text("${day.dayOfWeek.name.take(3)} ${day.dayOfMonth}") }
                )
            }
        }
    }
}

@Composable
private fun ViewSelector(view: AgendaView, inboxCount: Int, onView: (AgendaView) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AgendaView.entries.forEach { option ->
            FilterChip(
                selected = option == view,
                onClick = { onView(option) },
                label = { Text(if (option == AgendaView.INBOX && inboxCount > 0) "Bandeja $inboxCount" else option.label) }
            )
        }
    }
}

@Composable
private fun QuickCapture(state: AgendaUiState, viewModel: AgendaViewModel) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(if (state.view == AgendaView.INBOX) "Captura rápida" else "Siguiente acción", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = state.titleInput,
                onValueChange = viewModel::onTitleChanged,
                label = { Text("¿Qué quieres dejar preparado?") },
                placeholder = { Text("Ej. comprar ingredientes") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AgendaItemKind.entries.forEach { kind ->
                    FilterChip(selected = state.kind == kind, onClick = { viewModel.setKind(kind) }, label = { Text(kind.label) })
                }
            }
            if (state.kind == AgendaItemKind.REMINDER || state.kind == AgendaItemKind.EVENT) {
                OutlinedTextField(
                    value = state.timeInput,
                    onValueChange = viewModel::onTimeChanged,
                    label = { Text("Hora (HH:mm), opcional") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Button(onClick = { viewModel.addItem() }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text(if (state.view == AgendaView.INBOX) "Guardar en bandeja" else "Añadir a ${state.selectedDate}")
            }
        }
    }
}

@Composable
private fun TodaySummary(items: List<AgendaItem>) {
    val priorities = items.filterNot { it.completed }.take(3)
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Hoy en una página", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (priorities.isEmpty()) Text("No hay prioridades pendientes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            else priorities.forEachIndexed { index, item -> Text("${index + 1}. ${item.title}", fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Medium) }
        }
    }
}

@Composable
private fun AgendaItemCard(item: AgendaItem, projects: List<AgendaProject>, viewModel: AgendaViewModel) {
    val project = projects.firstOrNull { it.id == item.projectId }
    Card(shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = item.completed, onCheckedChange = { viewModel.toggle(item) }, modifier = Modifier.semantics { contentDescription = "Completar ${item.title}" })
            Icon(kindIcon(item.kind), item.kind.label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(21.dp))
            Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(item.title, fontWeight = FontWeight.Bold, textDecoration = if (item.completed) TextDecoration.LineThrough else null)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(item.kind.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (item.time.isNotBlank()) Text(item.time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    project?.let { Text(it.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
            IconButton(onClick = { viewModel.remove(item) }, modifier = Modifier.semantics { contentDescription = "Eliminar ${item.title}" }) {
                Icon(Icons.Default.Delete, "Eliminar")
            }
        }
    }
}

@Composable
private fun ProjectComposer(state: AgendaUiState, viewModel: AgendaViewModel) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("Proyectos con siguiente paso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(state.projectNameInput, viewModel::onProjectNameChanged, label = { Text("Nombre del proyecto") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(state.projectGoalInput, viewModel::onProjectGoalChanged, label = { Text("Resultado que quieres conseguir (opcional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(onClick = viewModel::addProject, modifier = Modifier.fillMaxWidth()) { Text("Crear proyecto") }
        }
    }
}

@Composable
private fun ProjectCard(project: AgendaProject, items: List<AgendaItem>, viewModel: AgendaViewModel) {
    val projectItems = items.filter { it.projectId == project.id }
    val progress = if (projectItems.isEmpty()) 0f else projectItems.count { it.completed }.toFloat() / projectItems.size
    Card(shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(project.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.removeProject(project) }, modifier = Modifier.semantics { contentDescription = "Eliminar proyecto ${project.name}" }) {
                    Icon(Icons.Default.Delete, "Eliminar proyecto")
                }
            }
            if (project.goal.isNotBlank()) Text(project.goal, color = MaterialTheme.colorScheme.onSurfaceVariant)
            androidx.compose.material3.LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            Text("${(progress * 100).toInt()} % · ${projectItems.count { !it.completed }} pendientes", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun EmptyAgenda(view: AgendaView) {
    Text(
        when (view) {
            AgendaView.TODAY -> "Tu día está despejado. Elige una siguiente acción."
            AgendaView.WEEK -> "No hay elementos en esta semana."
            AgendaView.INBOX -> "La bandeja está vacía. Captura aquí cualquier idea."
            AgendaView.PROJECTS -> "Crea un proyecto para agrupar pasos y ver su avance."
        },
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private fun kindIcon(kind: AgendaItemKind) = when (kind) {
    AgendaItemKind.TASK -> Icons.Default.Info
    AgendaItemKind.EVENT -> Icons.Default.Info
    AgendaItemKind.REMINDER -> Icons.Default.Info
    AgendaItemKind.HABIT -> Icons.Default.Info
}
