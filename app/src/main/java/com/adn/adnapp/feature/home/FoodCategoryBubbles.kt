package com.adn.adnapp.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.adn.adnapp.core.ui.CellSurface
import com.adn.adnapp.core.ui.FoodIllustration

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FoodCategoryBubbles(categories: List<String>, selected: String?, onSelect: (String) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val cellWidth = (maxWidth - 16.dp) / 3
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            maxItemsInEachRow = 3
        ) {
            categories.forEachIndexed { index, category ->
                Column(Modifier.width(cellWidth).padding(top = if (index % 3 == 1) 12.dp else 0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    CellSurface(onClick = { onSelect(category) },
                        modifier = Modifier.fillMaxWidth().height(cellWidth).semantics { contentDescription = "Categoría $category" },
                        variant = 4 + index % 3, selected = selected == category) {
                        FoodIllustration(category.illustration(), Modifier.fillMaxSize(.58f))
                    }
                    Text(category, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

private fun String.illustration() = when {
    contains("huevo", true) -> FoodIllustration.EGG
    contains("carne", true) || contains("jamón", true) -> FoodIllustration.FRESH
    contains("pesc", true) -> FoodIllustration.FISH
    contains("verdur", true) -> FoodIllustration.LEAF
    contains("fruta", true) -> FoodIllustration.FRUIT
    contains("láct", true) || contains("queso", true) -> FoodIllustration.DAIRY
    contains("cereal", true) || contains("legumbre", true) || contains("secos", true) -> FoodIllustration.GRAINS
    else -> FoodIllustration.PLATE
}
