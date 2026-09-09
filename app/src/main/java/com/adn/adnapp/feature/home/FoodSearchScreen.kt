package com.adn.adnapp.feature.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.adn.adnapp.data.model.entity.Product
import com.adn.adnapp.data.model.entity.ProductNutrient
import com.adn.adnapp.core.ui.OrganicBackButton
import com.adn.adnapp.core.ui.FoodIllustration
import com.adn.adnapp.core.ui.LocalFloatingNavigationInset
import com.adn.adnapp.core.ui.EntryDateSheet
import com.adn.adnapp.core.ui.readableDate
import java.util.Locale
import org.koin.androidx.compose.koinViewModel
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FoodSearchScreen(
    viewModel: HomeViewModel = koinViewModel(),
    initialSection: FoodSection = FoodSection.PRODUCTS,
    onBack: (() -> Unit)? = null,
    fridgeOnly: Boolean = false
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var infoProduct by remember { mutableStateOf<Product?>(null) }
    var freshQuery by rememberSaveable { mutableStateOf("") }
    var freshCategory by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    LaunchedEffect(initialSection) { viewModel.onFoodSectionChanged(initialSection) }
    val scanner = remember(context) {
        GmsBarcodeScanning.getClient(
            context,
            GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E
                )
                .enableAutoZoom()
                .build()
        )
    }

    Scaffold { padding ->
        Box(Modifier.fillMaxSize().statusBarsPadding()) {
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = if (onBack != null) 78.dp else 12.dp, bottom = LocalFloatingNavigationInset.current + 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!fridgeOnly) item { Text("Registro para " + state.screenDate.readableDate(),
                style = MaterialTheme.typography.labelMedium) }
            if (!fridgeOnly) item {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FoodSection.entries.forEach { section ->
                        FilterChip(
                            selected = state.foodSection == section,
                            onClick = { viewModel.onFoodSectionChanged(section) },
                            leadingIcon = { FoodIllustration(section.illustration(), Modifier.size(30.dp)) },
                            label = {},
                            modifier = Modifier.size(58.dp).semantics {
                                contentDescription = "Abrir ${section.label()}"
                            },
                        )
                    }
                }
            }
            when (state.foodSection) {
                FoodSection.PRODUCTS -> {
                    item {
                        Text(
                            "Busca productos envasados o escanea el código de barras.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = state.searchQuery,
                                onValueChange = viewModel::onSearchQueryChanged,
                                label = { Text("Producto o marca") },
                                placeholder = { Text("Ej. yogur natural Danone") },
                                singleLine = true,
                                enabled = !state.isSearching,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { viewModel.searchFood() }),
                                trailingIcon = { Icon(Icons.Default.Search, null) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = viewModel::searchFood,
                                    enabled = !state.isSearching,
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Icon(Icons.Default.Search, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Buscar")
                                }
                                FilledTonalIconButton(
                                    onClick = {
                                        scanner.startScan()
                                            .addOnSuccessListener { it.rawValue?.let(viewModel::onBarcodeScanned) }
                                            .addOnFailureListener { viewModel.onBarcodeScanFailed() }
                                    },
                                    enabled = !state.isSearching,
                                    modifier = Modifier.size(48.dp)
                                ) { BarcodeIcon() }
                            }
                        }
                    }
                }
                FoodSection.FRESH -> {
                    item {
                        Text("Alimentos sin marca · valores medios orientativos por 100 g.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    item {
                        OutlinedTextField(
                            value = freshQuery,
                            onValueChange = { freshQuery = it },
                            label = { Text("Filtrar alimento fresco") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        val categories = state.freshFoods.flatMap { it.categories.take(1) }.distinct()
                        if (freshCategory == null && freshQuery.isBlank()) {
                            FoodCategoryBubbles(categories, freshCategory) { freshCategory = it }
                        } else {
                            TextButton(onClick = { freshCategory = null; freshQuery = "" }) { Text("Todas las categorías") }
                            freshCategory?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
                        }
                    }
                }
                FoodSection.CUSTOM, FoodSection.SAVED -> {
                    item { FridgeHeader() }
                    item {
                        Text(when {
                            fridgeOnly -> "Tu enciclopedia local de alimentos usados, favoritos y platos propios. Toca una ficha para consultar toda su información."
                            state.foodSection == FoodSection.CUSTOM -> "Tus platos personalizados, guardados automáticamente como favoritos."
                            else -> "Tus alimentos favoritos. Toca una tarjeta para consultar todos sus nutrientes."
                        }, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (state.foodSection == FoodSection.PRODUCTS && state.searchQuery.isBlank() && state.searchResults.isEmpty() && !state.isSearching) item {
                FoodCategoryBubbles(listOf("Quesos", "Carnes", "Pescados", "Huevos", "Verduras", "Lácteos", "Frutas", "Cereales", "Legumbres", "Frutos secos"), null) {
                    viewModel.onSearchQueryChanged(it)
                    viewModel.searchFood()
                }
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
            val visibleProducts = when (state.foodSection) {
                FoodSection.PRODUCTS -> state.searchResults
                FoodSection.FRESH -> state.freshFoods.filter { product ->
                    (freshCategory == null || product.categories.firstOrNull() == freshCategory) &&
                        (freshQuery.isBlank() || product.name.contains(freshQuery, ignoreCase = true))
                }
                FoodSection.CUSTOM -> state.favoriteFoods.filter { it.code.startsWith("dish:") }
                FoodSection.SAVED -> if (fridgeOnly) {
                    (state.favoriteFoods + state.recentFoods).distinctBy { it.code }
                } else state.favoriteFoods.filterNot { it.code.startsWith("dish:") }
            }
            if (state.foodSection in listOf(FoodSection.CUSTOM, FoodSection.SAVED) && visibleProducts.isEmpty()) item {
                Text("Aún no tienes alimentos guardados. Marca el corazón de cualquier producto para encontrarlo aquí.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (fridgeOnly) item {
                FridgeGrid(visibleProducts, onInfo = { infoProduct = it })
            } else items(visibleProducts, key = { it.code }) { product ->
                if (fridgeOnly || state.foodSection == FoodSection.CUSTOM || state.foodSection == FoodSection.SAVED) {
                    FridgeProductCard(product, onInfo = { infoProduct = product })
                } else ProductResultCard(
                    product = product,
                    onInfo = { infoProduct = product },
                    onAdd = { viewModel.onProductSelected(product) },
                    isFavorite = product.code in state.favoriteCodes,
                    onFavorite = { viewModel.toggleFavorite(product) }
                )
            }
            if (state.foodSection == FoodSection.PRODUCTS && state.searchResults.isNotEmpty()) item {
                Text(
                    "Valores por 100 g · Fuente: Open Food Facts",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
            }
        }
        onBack?.let { OrganicBackButton(it, Modifier.align(Alignment.TopStart).padding(start = 10.dp, top = 8.dp)) }
        }
    }

    infoProduct?.let { product ->
        ProductInfoSheet(product = product, onDismiss = { infoProduct = null })
    }
    state.selectedProduct?.takeIf { state.dateChoice == null }?.let { product ->
        AddProductSheet(
            product = product, quantity = state.quantityToAdd,
            onQuantityChange = viewModel::onQuantityChanged,
            onAdd = viewModel::addFoodEntry,
            onDismiss = { viewModel.onProductSelectedDismissed() }
        )
    }
    state.dateChoice?.let {
        EntryDateSheet(it, viewModel::confirmEntryDate, viewModel::cancelDateChoice)
    }
}

@Composable
private fun BarcodeIcon() {
    val color = MaterialTheme.colorScheme.onSecondaryContainer
    Canvas(Modifier.size(23.dp)) {
        val widths = listOf(.08f, .14f, .07f, .12f, .06f, .15f, .08f)
        var x = size.width * .05f
        widths.forEachIndexed { index, fraction ->
            val width = size.width * fraction
            drawRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(x, if (index % 2 == 0) 0f else size.height * .12f),
                size = androidx.compose.ui.geometry.Size(width, if (index % 2 == 0) size.height else size.height * .76f)
            )
            x += width + size.width * .045f
        }
    }
}

@Composable
private fun ProductResultCard(
    product: Product,
    onInfo: () -> Unit,
    onAdd: () -> Unit,
    isFavorite: Boolean,
    onFavorite: () -> Unit
) {
    var flipped by rememberSaveable(product.code) { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (flipped) 180f else 0f, label = "product-card")
    Card(
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth().height(if (product.code.startsWith("dish:")) 196.dp else 238.dp).graphicsLayer {
            rotationY = rotation
            cameraDistance = 12f * density
        }
    ) {
        if (rotation <= 90f) {
            Row(Modifier.fillMaxSize()) {
                ProductImage(product, Modifier.weight(.3f))
                Column(Modifier.weight(.7f).fillMaxHeight().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text(product.name, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            if (product.brands.isNotBlank()) Text(product.brands, style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Row {
                            IconButton(onClick = onFavorite, modifier = Modifier.size(36.dp)) {
                                Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Favorito", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = onInfo, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Info, "Información nutricional", tint = MaterialTheme.colorScheme.primary)
                            }
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
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { flipped = true }, contentPadding = PaddingValues(horizontal = 5.dp)) { Text("Más nutrientes") }
                        TextButton(onClick = onAdd, contentPadding = PaddingValues(horizontal = 5.dp)) {
                            Icon(Icons.Default.Add, null, Modifier.size(18.dp)); Text("Añadir")
                        }
                    }
                }
            }
        } else {
            ProductCardBack(product, onFlipBack = { flipped = false })
        }
    }
}

@Composable
private fun FridgeProductCard(
    product: Product,
    onInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(shape = RoundedCornerShape(18.dp), modifier = modifier.fillMaxWidth().height(174.dp).clickable(onClick = onInfo)) {
        Column(Modifier.fillMaxSize().padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ProductImage(product, Modifier.fillMaxWidth().height(88.dp).clip(RoundedCornerShape(14.dp)))
            Text(product.name, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("${product.calories.pretty()} kcal · ${product.proteins.pretty()} g proteína", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun FridgeGrid(products: List<Product>, onInfo: (Product) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val cellWidth = (maxWidth - 16.dp) / 3
        FlowRow(
            Modifier.fillMaxWidth(),
            maxItemsInEachRow = 3,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            products.forEach { product ->
                FridgeProductCard(product, onInfo = { onInfo(product) }, modifier = Modifier.width(cellWidth))
            }
        }
    }
}

@Composable
private fun ProductImage(product: Product, modifier: Modifier) {
    Box(modifier.fillMaxHeight().background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
        if (product.imageUrl.isNullOrBlank()) {
            Icon(Icons.Default.Search, null, Modifier.size(42.dp), tint = MaterialTheme.colorScheme.primary)
        } else {
            AsyncImage(model = product.imageUrl, contentDescription = "Foto de ${product.name}", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun ProductCardBack(product: Product, onFlipBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Más nutrientes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            TextButton(onClick = onFlipBack) { Text("Volver") }
        }
        Text("Por 100 g", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (product.nutrients.isEmpty()) {
            Text("Sin datos adicionales en la fuente.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            product.nutrients.take(5).forEach { nutrient ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(nutrient.name, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${nutrient.amountPer100g.pretty()} ${nutrient.unit}", fontWeight = FontWeight.Bold)
                }
            }
        }
        if (product.ingredients.isNotBlank()) Text("Ingredientes: ${product.ingredients}", maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
        if (product.allergens.isNotEmpty()) Text("Alérgenos: ${product.allergens.joinToString()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun FridgeHeader() {
    Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("La nevera", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Tu enciclopedia personal de alimentos guardados y usados.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Métricas de tu biblioteca", fontWeight = FontWeight.Bold)
                    Text(if (product.code.startsWith("dish:")) "Plato propio · guardado automáticamente en Favoritos" else "Alimento favorito · disponible para añadir a tus comidas",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text("La etiqueta de calidad combina Nutri-Score y tu perfil de dieta.", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
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

private fun FoodSection.label() = when (this) {
    FoodSection.PRODUCTS -> "Súper"
    FoodSection.FRESH -> "Mercadillo"
    FoodSection.CUSTOM -> "Propios"
    FoodSection.SAVED -> "Favoritos"
}

private fun FoodSection.illustration() = when (this) {
    FoodSection.PRODUCTS -> FoodIllustration.CART
    FoodSection.FRESH -> FoodIllustration.FRESH
    FoodSection.CUSTOM -> FoodIllustration.PLATE
    FoodSection.SAVED -> FoodIllustration.HEART
}
