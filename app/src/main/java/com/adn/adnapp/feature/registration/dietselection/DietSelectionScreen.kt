package com.adn.adnapp.feature.registration.dietselection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.adn.adnapp.R
import com.adn.adnapp.core.ui.CompactTopBar
import com.adn.adnapp.data.model.entity.Diet
import com.adn.adnapp.domain.model.MacroTolerance
import kotlin.math.roundToInt
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietSelectionScreen(
    viewModel: DietSelectionViewModel = koinViewModel(),
    onNavigateToMain: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is DietSelectionEvent.NavigateToMain -> onNavigateToMain()
            }
        }
    }

    Scaffold(
        topBar = {
            CompactTopBar(stringResource(R.string.select_diet))
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Text(
                    "Elige un plan o define tus propios límites diarios.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { viewModel.showCustomDietCreator(true) },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) { Text("+ Crear dieta personalizada") }
                Spacer(Modifier.height(10.dp))
                MacroToleranceSelector(
                    value = uiState.macroTolerance,
                    onValueChange = viewModel::onMacroToleranceChanged
                )
                Spacer(Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.diets, key = { it.id }) { diet ->
                        DietCard(diet, uiState.selectedDietId == diet.id) {
                            viewModel.onDietSelected(diet.id)
                        }
                    }
                }
                
                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = viewModel::onCompleteClicked,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !uiState.isSaving && uiState.selectedDietId != null
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }

    if (uiState.showCustomDietCreator) {
        CustomDietDialog(uiState, viewModel)
    }
}

@Composable
private fun MacroToleranceSelector(
    value: MacroTolerance,
    onValueChange: (MacroTolerance) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text("Tolerancia al superar objetivos", fontWeight = FontWeight.Bold)
            Text(
                "Define cuándo un exceso de calorías, carbohidratos o grasas empieza a penalizar. La proteína no se penaliza por exceso.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Slider(
                value = value.ordinal.toFloat(),
                onValueChange = { index ->
                    onValueChange(MacroTolerance.entries[index.roundToInt().coerceIn(0, 2)])
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
                            else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
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
private fun DietCard(diet: Diet, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        border = if (selected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Image(
            painter = painterResource(dietImageRes(diet)),
            contentDescription = "Imagen de ${diet.name}",
            modifier = Modifier.fillMaxWidth().height(132.dp),
            contentScale = ContentScale.Crop
        )
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(diet.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (diet.id.startsWith("custom_")) {
                    Text("Personalizada", color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
            Text(diet.description, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "${diet.calories.toInt()} kcal  ·  P ${diet.proteins.toInt()} g  ·  C ${diet.carbs.toInt()} g  ·  G ${diet.lipids.toInt()} g",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CustomDietDialog(state: DietSelectionUiState, viewModel: DietSelectionViewModel) {
    AlertDialog(
        onDismissRequest = { if (!state.isCreatingDiet) viewModel.showCustomDietCreator(false) },
        title = { Text("Nueva dieta personalizada") },
        text = {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Define los objetivos diarios que usarán Nutrición y el calendario.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                CustomField(state.customDiet.name, viewModel::onCustomNameChanged, "Nombre", keyboardType = KeyboardType.Text)
                CustomField(state.customDiet.description, viewModel::onCustomDescriptionChanged, "Descripción", keyboardType = KeyboardType.Text)
                CustomField(state.customDiet.calories, viewModel::onCustomCaloriesChanged, "Calorías (kcal)")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CustomField(state.customDiet.proteins, viewModel::onCustomProteinsChanged, "Proteínas (g)", modifier = Modifier.weight(1f))
                    CustomField(state.customDiet.carbs, viewModel::onCustomCarbsChanged, "Carbos (g)", modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CustomField(state.customDiet.fats, viewModel::onCustomFatsChanged, "Grasas (g)", modifier = Modifier.weight(1f))
                    CustomField(state.customDiet.sugar, viewModel::onCustomSugarChanged, "Azúcar máx. (g)", modifier = Modifier.weight(1f))
                }
                CustomField(state.customDiet.water, viewModel::onCustomWaterChanged, "Agua (ml)")
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = viewModel::saveCustomDiet, enabled = !state.isCreatingDiet) {
                if (state.isCreatingDiet) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Crear y seleccionar")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.showCustomDietCreator(false) }, enabled = !state.isCreatingDiet) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun CustomField(
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
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}

private fun dietImageRes(diet: Diet): Int = when (diet.id) {
    "balanced" -> R.drawable.diet_balanced
    "high_protein" -> R.drawable.diet_high_protein
    "vegetarian" -> R.drawable.diet_vegetarian
    "low_carb" -> R.drawable.diet_low_carb
    else -> R.drawable.diet_custom
}
