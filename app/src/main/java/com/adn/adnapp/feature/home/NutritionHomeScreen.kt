package com.adn.adnapp.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.adn.adnapp.core.ui.*

@Composable
fun NutritionHomeScreen(
    onSuper: () -> Unit,
    onMarket: () -> Unit,
    onDish: () -> Unit,
    onSaved: () -> Unit,
    onWellness: () -> Unit = {}
) {
    Scaffold(topBar = { CompactTopBar("Nutrición") }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = LocalFloatingNavigationInset.current + 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("A tu gusto", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Elige de dónde viene tu plato", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FoodPortal("Súper", "Productos y marcas", FoodIllustration.CART, 4, onSuper, Modifier.weight(1f))
                FoodPortal("Mercadillo", "Alimentos frescos", FoodIllustration.FRESH, 5, onMarket, Modifier.weight(1f))
            }
            FoodPortal("Lo mejor de la casa", "Tus propios platos", FoodIllustration.PLATE, 6, onDish,
                Modifier.widthIn(max = 200.dp).fillMaxWidth())
            TextButton(onClick = onSaved) { Text("Abrir nevera") }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FoodPortal("Agua", "Hidratación", FoodIllustration.WATER, 7, onWellness, Modifier.weight(1f))
                FoodPortal("Sol", "Tiempo exterior", FoodIllustration.SUN, 8, onWellness, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FoodPortal(
    title: String,
    subtitle: String,
    illustration: FoodIllustration,
    variant: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        CellSurface(onClick, Modifier.fillMaxWidth().aspectRatio(1f).semantics { contentDescription = "Abrir $title" }, variant, selected = true) {
            FoodIllustration(illustration, Modifier.fillMaxSize(.52f))
        }
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
