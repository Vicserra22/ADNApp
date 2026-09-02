package com.adn.adnapp.feature.dayviewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adn.adnapp.data.model.entity.FoodEntry
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
    var pendingDelete by remember { mutableStateOf<FoodEntry?>(null) }

    pendingDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Eliminar entrada") },
            text = { Text("Se descontarán del total diario los valores de ${entry.name}.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteEntry(entry); pendingDelete = null }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancelar") } }
        )
    }

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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.isLoading) CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            else {
                DayRating(state)
                TargetSummary(state)
                SugarDanger(state)
                WaterCard(state, viewModel)
            }

            if (state.isEditing) EntryEditor(state, viewModel)
            else Button(onClick = viewModel::startEditing, modifier = Modifier.fillMaxWidth()) {
                Text("Añadir macros manualmente")
            }

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            state.savedMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) }

            Text("Registro del día", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Cada alimento, ajuste manual y vaso de agua queda registrado por separado.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            when {
                state.areEntriesLoading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                state.entries.isEmpty() -> EmptyEntries()
                else -> state.entries.forEach { entry ->
                    EntryCard(
                        entry = entry,
                        isDeleting = state.deletingEntryId == entry.id,
                        onEdit = { viewModel.startEditingEntry(entry) },
                        onDelete = { pendingDelete = entry }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DayRating(state: DayViewerUiState) {
    val score = state.score ?: return
    val percent = score.percent
    val (label, color) = when {
        percent >= 85 -> "Día excelente" to Color(0xFF16834A)
        percent >= 65 -> "Buen progreso" to Color(0xFF5C8D2B)
        percent >= 40 -> "Puedes acercarte más" to Color(0xFFE09A22)
        else -> "Lejos de tus objetivos" to Color(0xFFC74343)
    }
    Card(colors = CardDefaults.cardColors(containerColor = color.copy(alpha = .12f))) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("$percent%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = color)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(label, fontWeight = FontWeight.Bold)
                Text("Cumplimiento global del día", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TargetSummary(state: DayViewerUiState) {
    val target = state.targets
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Nutrición", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TargetBar("Calorías", state.consumption.calories, target?.calories, "kcal")
            TargetBar("Proteínas", state.consumption.proteins, target?.proteins, "g")
            TargetBar("Carbohidratos", state.consumption.carbs, target?.carbs, "g")
            TargetBar("Grasas", state.consumption.fats, target?.fats, "g")
        }
    }
}

@Composable
private fun TargetBar(label: String, actual: Double, target: Double?, unit: String) {
    val validTarget = target?.takeIf { it > 0 }
    val progress = if (validTarget == null) 0f else (actual / validTarget).toFloat().coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label)
            Text(
                if (validTarget == null) "${actual.clean()} $unit"
                else "${actual.clean()} / ${validTarget.clean()} $unit",
                fontWeight = FontWeight.SemiBold
            )
        }
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(8.dp)))
    }
}

@Composable
private fun SugarDanger(state: DayViewerUiState) {
    val maximum = state.targets?.sugarMax?.takeIf { it > 0 } ?: 50.0
    val ratio = state.consumption.sugar / maximum
    val color = when {
        ratio < .65 -> Color(0xFF2E9B57)
        ratio < .9 -> Color(0xFFE6A324)
        else -> Color(0xFFD33F3F)
    }
    Card(colors = CardDefaults.cardColors(containerColor = color.copy(alpha = .12f))) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Azúcar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${state.consumption.sugar.clean()} / ${maximum.clean()} g", color = color, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { ratio.toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(12.dp)),
                color = color,
                trackColor = color.copy(alpha = .18f)
            )
            Text(
                when {
                    ratio >= 1 -> "Límite diario alcanzado. Prioriza alimentos sin azúcares añadidos."
                    ratio >= .8 -> "Te estás acercando al límite diario."
                    else -> "Mantén el consumo por debajo del límite."
                },
                color = color
            )
        }
    }
}

@Composable
private fun WaterCard(state: DayViewerUiState, viewModel: DayViewerViewModel) {
    val target = state.targets?.waterMl?.takeIf { it > 0 } ?: 2_000.0
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .55f))) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Agua", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TargetBar("Hidratación", state.consumption.waterMl, target, "ml")
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = state.waterMl,
                    onValueChange = viewModel::onWaterChanged,
                    label = { Text("Cantidad (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = viewModel::addWater, enabled = !state.isSaving) { Text("Añadir") }
            }
        }
    }
}

@Composable
private fun EntryEditor(state: DayViewerUiState, viewModel: DayViewerViewModel) {
    val isWater = state.editingEntry?.kind == FoodEntry.KIND_WATER
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .45f))) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                if (state.editingEntry == null) "Nueva entrada manual" else "Editar ${state.editingEntry.name}",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
            )
            Text(
                "Se guardará como una entrada independiente y el total se recalculará automáticamente.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (state.editingEntry != null) {
                MacroField("Nombre", state.name, viewModel::onNameChanged, KeyboardType.Text)
                if (!isWater) MacroField("Cantidad (g)", state.quantity, viewModel::onQuantityChanged)
            }
            if (isWater) MacroField("Agua (ml)", state.waterMl, viewModel::onWaterChanged)
            else {
                MacroField("Calorías (kcal)", state.calories, viewModel::onCaloriesChanged)
                MacroField("Proteínas (g)", state.proteins, viewModel::onProteinsChanged)
                MacroField("Carbohidratos (g)", state.carbs, viewModel::onCarbsChanged)
                MacroField("Grasas (g)", state.fats, viewModel::onFatsChanged)
                MacroField("Azúcar (g)", state.sugar, viewModel::onSugarChanged)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = viewModel::cancelEditing, Modifier.weight(1f), enabled = !state.isSaving) { Text("Cancelar") }
                Button(onClick = viewModel::saveManualMacros, Modifier.weight(1f), enabled = !state.isSaving) {
                    if (state.isSaving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Text("Guardar")
                }
            }
        }
    }
}

@Composable
private fun EntryCard(entry: FoodEntry, isDeleting: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(42.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(when (entry.kind) { FoodEntry.KIND_WATER -> "💧"; FoodEntry.KIND_MANUAL -> "±"; else -> "●" })
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(entry.name, fontWeight = FontWeight.Bold)
                Text(entry.entrySummary(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onEdit, enabled = !isDeleting) { Icon(Icons.Default.Edit, "Editar ${entry.name}") }
            IconButton(onClick = onDelete, enabled = !isDeleting) {
                if (isDeleting) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Icon(Icons.Default.Delete, "Eliminar ${entry.name}", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun EmptyEntries() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f))) {
        Text("Todavía no hay entradas para este día.", Modifier.fillMaxWidth().padding(22.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MacroField(label: String, value: String, onValueChange: (String) -> Unit, type: KeyboardType = KeyboardType.Decimal) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        modifier = Modifier.fillMaxWidth(), singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = type)
    )
}

private fun FoodEntry.entrySummary(): String = when (kind) {
    FoodEntry.KIND_WATER -> "${waterMl.clean()} ml"
    else -> buildList {
        if (quantity > 0) add("${quantity.clean()} g")
        add("${calories.clean()} kcal")
        if (sugar > 0) add("azúcar ${sugar.clean()} g")
    }.joinToString(" · ")
}

private fun formatDate(value: String): String = runCatching {
    LocalDate.parse(value).format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "ES")))
        .replaceFirstChar { it.titlecase(Locale("es", "ES")) }
}.getOrDefault(value)

private fun Double.clean() = if (this % 1.0 == 0.0) toInt().toString() else "%.1f".format(this)
