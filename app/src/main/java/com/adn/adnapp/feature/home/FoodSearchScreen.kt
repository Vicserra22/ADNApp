package com.adn.adnapp.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.adn.adnapp.data.model.entity.Product
import com.adn.adnapp.data.model.entity.ProductNutrient
import java.util.Locale
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSearchScreen(viewModel: HomeViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var infoProduct by remember { mutableStateOf<Product?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text("Buscar alimentos") }) }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Consulta productos en Open Food Facts. La información es colaborativa y puede estar incompleta.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    label = { Text("Producto o marca") },
                    placeholder = { Text("Ej. yogur natural Danone") },
                    singleLine = true,
                    enabled = !state.isSearching,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { viewModel.searchFood() }),
                    trailingIcon = {
                        IconButton(onClick = viewModel::searchFood, enabled = !state.isSearching) {
                            Icon(Icons.Default.Search, "Buscar")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (state.isSearching) item {
                Row(Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error?.let { message -> item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(message, Modifier.fillMaxWidth().padding(14.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            } }
            state.successMessage?.let { message -> item {
                Text(message, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            } }
            items(state.searchResults, key = { it.code }) { product ->
                ProductResultCard(
                    product = product,
                    onInfo = { infoProduct = product },
                    onAdd = { viewModel.onProductSelected(product) }
                )
            }
            if (state.searchResults.isNotEmpty()) item {
                Text(
                    "Valores por 100 g · Fuente: Open Food Facts",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
            }
        }
    }

    infoProduct?.let { product ->
        ProductInfoSheet(product = product, onDismiss = { infoProduct = null })
    }
    state.selectedProduct?.let { product ->
        AddProductSheet(
            product = product, quantity = state.quantityToAdd,
            onQuantityChange = viewModel::onQuantityChanged,
            onAdd = viewModel::addFoodEntry,
            onDismiss = { viewModel.onProductSelectedDismissed() }
        )
    }
}

@Composable
private fun ProductResultCard(product: Product, onInfo: () -> Unit, onAdd: () -> Unit) {
    Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth().height(220.dp)) {
        Row(Modifier.fillMaxSize()) {
            Box(
                Modifier.weight(.3f).fillMaxHeight().background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl.isNullOrBlank()) {
                    Icon(Icons.Default.Search, null, Modifier.size(42.dp), tint = MaterialTheme.colorScheme.primary)
                } else {
                    AsyncImage(
                        model = product.imageUrl, contentDescription = "Foto de ${product.name}",
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Column(
                Modifier.weight(.7f).fillMaxHeight().padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(product.name, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (product.brands.isNotBlank()) Text(product.brands, style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(onClick = onInfo, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Info, "Información nutricional", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MacroChip("Prot", product.proteins, "g", Modifier.weight(1f))
                    MacroChip("Carbos", product.carbs, "g", Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MacroChip("Grasas", product.fats, "g", Modifier.weight(1f))
                    MacroChip("Energía", product.calories, "kcal", Modifier.weight(1f))
                }
                TextButton(onClick = onAdd, modifier = Modifier.align(Alignment.End).height(34.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp)); Text("Añadir")
                }
            }
        }
    }
}

@Composable
private fun MacroChip(label: String, value: Double, unit: String, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(Modifier.padding(horizontal = 7.dp, vertical = 5.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${value.pretty()} $unit", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                maxLines = 1)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductInfoSheet(product: Product, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = product.imageUrl, contentDescription = null, contentScale = ContentScale.Crop,
                    modifier = Modifier.size(76.dp).clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
                Column(Modifier.padding(start = 14.dp).weight(1f)) {
                    Text(product.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (product.brands.isNotBlank()) Text(product.brands, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (product.quantity.isNotBlank()) Text(product.quantity, style = MaterialTheme.typography.labelLarge)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (product.nutritionGrade.isNotBlank()) QualityBadge("Nutri-Score ${product.nutritionGrade}", nutriColor(product.nutritionGrade))
                product.novaGroup?.let { QualityBadge("NOVA $it", novaColor(it)) }
                if (product.servingSize.isNotBlank()) BadgeText("Ración ${product.servingSize}")
            }
            Text("Nutrientes por 100 g", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (product.nutrients.isEmpty()) {
                Text("Este producto no tiene una tabla nutricional completa.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else product.nutrients.take(30).forEachIndexed { index, nutrient ->
                NutrientRow(nutrient, index, product.nutrients.size)
            }
            if (product.ingredients.isNotBlank()) InfoBlock("Ingredientes", product.ingredients)
            if (product.allergens.isNotEmpty()) InfoBlock("Alérgenos declarados", product.allergens.joinToString())
            if (product.categories.isNotEmpty()) InfoBlock("Categorías", product.categories.joinToString())
            Text(
                "Los datos son colaborativos. Comprueba siempre el envase, especialmente ante alergias.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun NutrientRow(nutrient: ProductNutrient, index: Int, count: Int) {
    val prominence = nutrient.dailyValuePercent?.div(100.0)?.toFloat()?.coerceIn(0f, 1f)
        ?: (1f - index.toFloat() / count.coerceAtLeast(1)).coerceIn(0f, 1f)
    val accent = if (nutrient.id in cautionNutrients) {
        lerp(Color(0xFFF2C94C), Color(0xFFC43E3E), prominence)
    } else {
        lerp(Color(0xFFF2C94C), Color(0xFF238B57), prominence)
    }
    Surface(shape = RoundedCornerShape(14.dp), color = accent.copy(alpha = .13f)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(accent, RoundedCornerShape(8.dp)))
            Text(nutrient.name, Modifier.weight(1f).padding(start = 9.dp), fontWeight = if (prominence > .7f) FontWeight.Bold else FontWeight.Medium)
            Column(horizontalAlignment = Alignment.End) {
                Text("${nutrient.amountPer100g.pretty()} ${nutrient.unit}", fontWeight = FontWeight.Bold)
                nutrient.dailyValuePercent?.let { Text("${it.pretty()}% VRN", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable private fun BadgeText(text: String) = Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
    Text(text, Modifier.padding(horizontal = 9.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer)
}

@Composable private fun QualityBadge(text: String, color: Color) = Surface(shape = RoundedCornerShape(12.dp), color = color.copy(alpha = .2f)) {
    Text(text, Modifier.padding(horizontal = 9.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium,
        color = color, fontWeight = FontWeight.Bold)
}

private fun nutriColor(grade: String) = when (grade.uppercase()) {
    "A" -> Color(0xFF198754); "B" -> Color(0xFF5B8C35); "C" -> Color(0xFF9A7B00)
    "D" -> Color(0xFFC26718); else -> Color(0xFFB43838)
}
private fun novaColor(group: Int) = when (group) {
    1 -> Color(0xFF198754); 2 -> Color(0xFF7A861E); 3 -> Color(0xFFC26718); else -> Color(0xFFB43838)
}
private val cautionNutrients = setOf("sugars", "saturated-fat", "salt", "sodium", "trans-fat")

@Composable private fun InfoBlock(title: String, value: String) = Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(title, fontWeight = FontWeight.Bold); Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddProductSheet(
    product: Product,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    onAdd: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Añadir ${product.name}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Los valores se calcularán proporcionalmente desde los datos por 100 g.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = quantity, onValueChange = onQuantityChange, label = { Text("Cantidad (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = onAdd, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Guardar alimento") }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun Double.pretty(): String = if (this % 1.0 == 0.0) toInt().toString()
else String.format(Locale.getDefault(), "%.1f", this)
