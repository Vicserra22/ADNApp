package com.adn.adnapp.feature.profile

import com.adn.adnapp.core.ui.LocalFloatingNavigationInset

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adn.adnapp.R
import com.adn.adnapp.core.ui.CompactTopBar
import com.adn.adnapp.core.ui.OrganicBackButton
import com.adn.adnapp.data.model.entity.UserProfile
import com.adn.adnapp.data.model.entity.WeightEntry
import com.adn.adnapp.domain.model.BodyGoal
import com.adn.adnapp.domain.model.Importance
import com.adn.adnapp.domain.model.MacroTolerance
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
    onNavigateToSplash: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showWeightDatePicker by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { if (it is ProfileEvent.NavigateToSplash) onNavigateToSplash() }
    }

    Scaffold { padding ->
        Box(Modifier.fillMaxSize()) {
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.userProfile == null -> ProfileError(
                message = state.error ?: "No se pudo mostrar el perfil",
                retry = viewModel::retry,
                modifier = Modifier.padding(padding)
            )
            else -> Column(
                Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState())
                    .padding(start = 18.dp, end = 18.dp, top = 78.dp, bottom = LocalFloatingNavigationInset.current + 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ProfileHeader(state.userProfile!!, state.isEditing, viewModel::startEditing)
                state.message?.let {
                    MessageCard(it)
                    LaunchedEffect(it) { kotlinx.coroutines.delay(2500); viewModel.clearMessage() }
                }
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (state.isEditing) EditableProfile(state, viewModel) else ProfileSummary(state)
                WeightProgressCard(state.weightHistory, state.userProfile!!)
                AddWeightCard(
                    state = state, onWeightChange = viewModel::onNewWeightChanged,
                    onPickDate = { showWeightDatePicker = true }, onSave = viewModel::addWeight
                )
                OutlinedButton(
                    onClick = viewModel::onLogoutClicked, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.logout)) }
                Spacer(Modifier.height(10.dp))
            }
        }
        onBack?.let { OrganicBackButton(it, Modifier.align(Alignment.TopStart).padding(start = 10.dp, top = 8.dp)) }
        }
    }

    if (showWeightDatePicker) {
        val selected = runCatching { LocalDate.parse(state.newWeightDate) }.getOrDefault(LocalDate.now())
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = selected.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis <= LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            }
        )
        DatePickerDialog(
            onDismissRequest = { showWeightDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        viewModel.onNewWeightDateChanged(
                            Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
                        )
                    }
                    showWeightDatePicker = false
                }) { Text("Elegir") }
            },
            dismissButton = { TextButton(onClick = { showWeightDatePicker = false }) { Text("Cancelar") } }
        ) { DatePicker(pickerState) }
    }
}

@Composable
private fun ProfileHeader(profile: UserProfile, editing: Boolean, onEdit: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(58.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(profile.name.trim().firstOrNull()?.uppercase() ?: "A", color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(profile.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(profile.bodyGoal.label(), color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            if (!editing) TextButton(onClick = onEdit) { Text("Editar") }
        }
    }
}

@Composable
private fun ProfileSummary(state: ProfileUiState) {
    val profile = state.userProfile ?: return
    Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Datos que personalizan tu plan", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
            InfoRow("Edad y sexo", "${profile.age} años · ${profile.gender}")
            InfoRow("Medidas", "${profile.weight.format()} kg · ${profile.height.format()} cm")
            InfoRow("Peso objetivo", "${profile.targetWeight.format()} kg")
            InfoRow("Dieta", state.diets.firstOrNull { it.id == state.selectedDietId }?.name ?: "Sin seleccionar")
            InfoRow("Tolerancia de macros", state.macroTolerance.label())
            InfoRow("Prioridades", "Nutrición ${profile.nutritionImportance.label().lowercase()}, deporte ${profile.sportsImportance.label().lowercase()}")
        }
    }
}

@Composable
private fun EditableProfile(state: ProfileUiState, viewModel: ProfileViewModel) {
    Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Editar perfil", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            ProfileField(state.name, viewModel::onNameChanged, "Nombre")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfileField(state.age, viewModel::onAgeChanged, "Edad", KeyboardType.Number, Modifier.weight(1f))
                ProfileField(state.height, viewModel::onHeightChanged, "Altura (cm)", KeyboardType.Decimal, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfileField(state.weight, viewModel::onWeightChanged, "Peso (kg)", KeyboardType.Decimal, Modifier.weight(1f))
                ProfileField(state.targetWeight, viewModel::onTargetWeightChanged, "Objetivo (kg)", KeyboardType.Decimal, Modifier.weight(1f))
            }
            Text("Sexo", fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Hombre", "Mujer", "Otro").forEach { gender ->
                    FilterChip(selected = state.gender == gender, onClick = { viewModel.onGenderChanged(gender) },
                        label = { Text(gender) }, modifier = Modifier.weight(1f))
                }
            }
            Text("Objetivo corporal", fontWeight = FontWeight.SemiBold)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BodyGoal.entries.forEach { goal ->
                    FilterChip(selected = state.bodyGoal == goal, onClick = { viewModel.onBodyGoalChanged(goal) },
                        label = { Text(goal.label()) }, modifier = Modifier.fillMaxWidth())
                }
            }
            if (state.diets.isNotEmpty()) {
                Text("Dieta", fontWeight = FontWeight.SemiBold)
                state.diets.forEach { diet ->
                    FilterChip(selected = state.selectedDietId == diet.id, onClick = { viewModel.onDietChanged(diet.id) },
                        label = { Text(diet.name) }, modifier = Modifier.fillMaxWidth())
                }
                MacroToleranceEditor(state.macroTolerance, viewModel::onMacroToleranceChanged)
            }
            Text("Importancia para tu valoración diaria", fontWeight = FontWeight.SemiBold)
            ImportanceEditor("Nutrición", state.nutritionImportance, viewModel::onNutritionImportanceChanged)
            ImportanceEditor("Deporte", state.sportsImportance, viewModel::onSportsImportanceChanged)
            ImportanceEditor("Objetivos personales", state.goalsImportance, viewModel::onGoalsImportanceChanged)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = viewModel::cancelEditing, modifier = Modifier.weight(1f), enabled = !state.isSaving) {
                    Text("Cancelar")
                }
                Button(onClick = viewModel::saveProfile, modifier = Modifier.weight(1f), enabled = !state.isSaving) {
                    if (state.isSaving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text("Guardar")
                }
            }
        }
    }
}

@Composable
private fun MacroToleranceEditor(
    value: MacroTolerance,
    onValueChange: (MacroTolerance) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("Tolerancia al superar objetivos", fontWeight = FontWeight.SemiBold)
        Text(
            "Ajusta cuándo el exceso de calorías, carbohidratos y grasas reduce tu valoración.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = value.ordinal.toFloat(),
            onValueChange = {
                onValueChange(MacroTolerance.entries[it.roundToInt().coerceIn(0, 2)])
            },
            valueRange = 0f..2f,
            steps = 1
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MacroTolerance.entries.forEach { option ->
                Text(
                    option.label(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (option == value) FontWeight.Bold else FontWeight.Normal,
                    color = if (option == value) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun MacroTolerance.label() = when (this) {
    MacroTolerance.PERMISSIVE -> "Permisivo"
    MacroTolerance.NORMAL -> "Normal"
    MacroTolerance.STRICT -> "Estricto"
}

@Composable
private fun WeightProgressCard(history: List<WeightEntry>, profile: UserProfile) {
    Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Evolución del peso", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("Actual", style = MaterialTheme.typography.labelMedium); Text("${profile.weight.format()} kg", fontWeight = FontWeight.Bold) }
                Column(horizontalAlignment = Alignment.End) { Text("Objetivo", style = MaterialTheme.typography.labelMedium); Text("${profile.targetWeight.format()} kg", fontWeight = FontWeight.Bold) }
            }
            val initial = history.firstOrNull()?.kilograms ?: profile.weight
            val distance = kotlin.math.abs(initial - profile.targetWeight)
            val covered = kotlin.math.abs(initial - profile.weight)
            LinearProgressIndicator(
                progress = { if (distance == 0.0) 1f else (covered / distance).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp)
            )
            if (history.size >= 2) WeightChart(history.takeLast(12))
            else Text("Registra al menos dos pesos para ver la gráfica.", style = MaterialTheme.typography.bodySmall)
            history.takeLast(4).reversed().forEach { entry ->
                InfoRow(entry.date.displayDate(), "${entry.kilograms.format()} kg")
            }
        }
    }
}

@Composable
private fun WeightChart(entries: List<WeightEntry>) {
    val color = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.primaryContainer
    Canvas(Modifier.fillMaxWidth().height(150.dp).background(surface.copy(alpha = .35f), RoundedCornerShape(16.dp)).padding(12.dp)) {
        val values = entries.map { it.kilograms }
        val min = (values.minOrNull() ?: 0.0) - 1.0
        val max = (values.maxOrNull() ?: 1.0) + 1.0
        val span = (max - min).coerceAtLeast(1.0)
        val stepX = size.width / (entries.size - 1).coerceAtLeast(1)
        val points = values.mapIndexed { index, value ->
            Offset(index * stepX, size.height - (((value - min) / span).toFloat() * size.height))
        }
        val path = Path().apply {
            points.forEachIndexed { index, point -> if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y) }
        }
        drawPath(path, color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx()))
        points.forEach { drawCircle(color, radius = 5.dp.toPx(), center = it) }
    }
}

@Composable
private fun AddWeightCard(state: ProfileUiState, onWeightChange: (String) -> Unit, onPickDate: () -> Unit, onSave: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Registrar peso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Una medición por día. Si repites una fecha, se corrige esa medición.", style = MaterialTheme.typography.bodySmall)
            ProfileField(state.newWeight, onWeightChange, "Peso (kg)", KeyboardType.Decimal)
            OutlinedButton(onClick = onPickDate, modifier = Modifier.fillMaxWidth()) { Text(state.newWeightDate.displayDate()) }
            Button(onClick = onSave, enabled = !state.isAddingWeight, modifier = Modifier.fillMaxWidth()) {
                if (state.isAddingWeight) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Añadir a la evolución")
            }
        }
    }
}

@Composable
private fun ImportanceEditor(label: String, value: Importance, onChange: (Importance) -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label); Text(value.label(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
        }
        Slider(value = value.weight.toFloat(), onValueChange = { raw ->
            onChange(Importance.entries[(raw.toInt() - 1).coerceIn(0, 4)])
        }, valueRange = 1f..5f, steps = 3)
    }
}

@Composable
private fun ProfileField(value: String, onValueChange: (String) -> Unit, label: String,
    keyboardType: KeyboardType = KeyboardType.Text, modifier: Modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(value, onValueChange, label = { Text(label) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType), modifier = modifier)
}

@Composable private fun InfoRow(label: String, value: String) = Row(
    Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top
) {
    Text(label, Modifier.weight(.38f), color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(
        value,
        Modifier.weight(.62f).padding(start = 12.dp),
        fontWeight = FontWeight.Medium,
        textAlign = androidx.compose.ui.text.style.TextAlign.End
    )
}
@Composable private fun MessageCard(message: String) = Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
    Text(message, Modifier.fillMaxWidth().padding(12.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
}
@Composable private fun ProfileError(message: String, retry: () -> Unit, modifier: Modifier = Modifier) = Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(message, color = MaterialTheme.colorScheme.error); Button(onClick = retry) { Text("Reintentar") }
    }
}

private fun BodyGoal.label() = when (this) { BodyGoal.LOSE_WEIGHT -> "Adelgazar"; BodyGoal.MAINTAIN -> "Mantener peso"; BodyGoal.GAIN_WEIGHT -> "Ganar peso" }
private fun Importance.label() = when (this) { Importance.VERY_LOW -> "Muy poco"; Importance.LOW -> "Poco"; Importance.NORMAL -> "Normal"; Importance.IMPORTANT -> "Importante"; Importance.VERY_IMPORTANT -> "Muy importante" }
private fun Double.format() = String.format(Locale.getDefault(), "%.1f", this)
private fun String.displayDate(): String = runCatching { LocalDate.parse(this).format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "ES"))) }.getOrDefault(this)
