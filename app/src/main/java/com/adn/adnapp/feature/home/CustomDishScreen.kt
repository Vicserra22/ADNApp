package com.adn.adnapp.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adn.adnapp.core.ui.*
import com.adn.adnapp.domain.model.toProduct
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@Composable
fun CustomDishScreen(onBack: () -> Unit, viewModel: CustomDishViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CustomDishContent(state, onBack, viewModel::onNameChanged, viewModel::onWeightChanged,
        viewModel::addIngredient, viewModel::removeIngredient, viewModel::saveDish)
}

@Composable
internal fun CustomDishContent(
    state: CustomDishUiState,
    onBack: () -> Unit,
    onName: (String) -> Unit,
    onWeight: (String) -> Unit,
    onIngredient: (com.adn.adnapp.data.model.entity.Product, String) -> Unit,
    onRemove: (String) -> Unit,
    onSave: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCode by rememberSaveable { mutableStateOf<String?>(null) }
    var grams by rememberSaveable { mutableStateOf("") }
    val selected = state.foods.firstOrNull { it.code == selectedCode }
    Scaffold { padding ->
        Box(Modifier.fillMaxSize().statusBarsPadding()) {
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 78.dp, bottom = LocalFloatingNavigationInset.current + 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FoodIllustration(FoodIllustration.PLATE, Modifier.size(64.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Hecho por ti", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("De una ensalada a tu paella", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item { OutlinedTextField(state.draft.name, onName, Modifier.fillMaxWidth(), label = { Text("Nombre del plato") }, singleLine = true, enabled = !state.isSaving) }
            item {
                Text("Ingredientes", style = MaterialTheme.typography.titleMedium)
                Text("Usa el alimento tal como lo pesas: crudo o cocinado. Los productos del Súper aparecen aquí al guardarlos como favoritos.", style = MaterialTheme.typography.bodySmall)
            }
            items(state.draft.ingredients, key = { it.product.code }) { ingredient ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("${ingredient.product.name} · ${ingredient.grams} g", Modifier.weight(1f))
                    TextButton(onClick = { selectedCode = ingredient.product.code; grams = ingredient.grams.toString() }, enabled = !state.isSaving) { Text("Editar") }
                    TextButton(onClick = { onRemove(ingredient.product.code) }, enabled = !state.isSaving) { Text("Quitar") }
                }
            }
            item {
                OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text("Buscar ingrediente") }, singleLine = true)
            }
            if (query.isNotBlank()) {
                val matches = state.foods.filter { it.name.contains(query, ignoreCase = true) }.take(12)
                if (matches.isEmpty()) item { Text("No hay coincidencias en frescos y favoritos.") }
                items(matches, key = { "choice:${it.code}" }) { product ->
                    TextButton(onClick = { selectedCode = product.code; grams = "" }, enabled = !state.isSaving,
                        modifier = Modifier.fillMaxWidth()) { Text(product.name, Modifier.fillMaxWidth()) }
                }
            }
            item {
                OutlinedTextField(state.draft.finalWeight, onWeight, Modifier.fillMaxWidth(),
                    label = { Text("Peso final listo para comer (g)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), enabled = !state.isSaving)
                Text("Pesa todo el plato sin el recipiente. El agua absorbida o evaporada cambia los valores por 100 g.", style = MaterialTheme.typography.bodySmall)
            }
            runCatching { state.draft.toProduct("preview") }.getOrNull()?.let { product ->
                item {
                    Text("Estimación por 100 g", fontWeight = FontWeight.Bold)
                    Text(String.format(Locale.getDefault(), "%.0f kcal · %.1f g proteína · %.1f g hidratos · %.1f g grasa",
                        product.calories, product.proteins, product.carbs, product.fats))
                }
            }
            state.error?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error) } }
            if (state.saved) item { Text("Plato guardado en Mis alimentos. Desde allí puedes añadir la cantidad que comas.", color = MaterialTheme.colorScheme.primary) }
            item { Button(onSave, Modifier.fillMaxWidth(), enabled = !state.isSaving) { Text(if (state.isSaving) "Guardando…" else "Guardar mi plato") } }
        }
        OrganicBackButton(onBack, Modifier.align(Alignment.TopStart).padding(start = 10.dp, top = 8.dp))
        }
    }
    selected?.let { product ->
        AlertDialog(
            onDismissRequest = { selectedCode = null },
            title = { Text(product.name) },
            text = { OutlinedTextField(grams, { grams = it }, label = { Text("Cantidad (g)") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)) },
            confirmButton = {
                val amount = grams.replace(',', '.').toDoubleOrNull()
                TextButton(onClick = { onIngredient(product, grams); selectedCode = null; query = "" },
                    enabled = amount != null && amount.isFinite() && amount > 0 && !state.isSaving) { Text("Añadir al plato") }
            },
            dismissButton = { TextButton(onClick = { selectedCode = null }) { Text("Cancelar") } }
        )
    }
}
