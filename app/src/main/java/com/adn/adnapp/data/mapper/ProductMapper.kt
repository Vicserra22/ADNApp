package com.adn.adnapp.data.mapper

import com.adn.adnapp.data.model.dto.ProductDto
import com.adn.adnapp.data.model.dto.SearchFoodHit
import com.adn.adnapp.data.model.entity.Product
import com.adn.adnapp.data.model.entity.ProductNutrient
import com.google.gson.JsonObject

object ProductMapper {
    fun mapToDomain(hit: SearchFoodHit): Product = mapToDomain(
        ProductDto(
            code = hit.code,
            name = hit.spanishName ?: hit.name ?: hit.spanishGenericName,
            smallImageUrl = hit.smallImageUrl,
            imageUrl = hit.imageUrl,
            brands = hit.brands?.joinToString(),
            quantity = hit.quantity,
            servingSize = hit.servingSize,
            ingredients = hit.spanishIngredients ?: hit.ingredients,
            allergens = hit.allergens,
            categories = hit.categories,
            nutritionGrade = hit.nutriScoreGrade ?: hit.nutritionGrade,
            novaGroup = hit.novaGroup,
            nutriments = hit.nutriments
        )
    )

    fun mapToDomain(dto: ProductDto): Product {
        val nutrients = dto.nutriments.toNutrients()
        return Product(
            code = dto.code ?: "",
            name = dto.name?.trim().orEmpty(),
            imageUrl = dto.smallImageUrl ?: dto.imageUrl,
            calories = nutrients.amount("energy-kcal"),
            fats = nutrients.amount("fat"),
            proteins = nutrients.amount("proteins"),
            carbs = nutrients.amount("carbohydrates"),
            sugars = nutrients.amount("sugars"),
            brands = dto.brands.orEmpty(), quantity = dto.quantity.orEmpty(),
            servingSize = dto.servingSize.orEmpty(), ingredients = dto.ingredients.orEmpty(),
            allergens = dto.allergens.orEmpty().map(::cleanTag),
            categories = dto.categories.orEmpty().take(4).map(::cleanTag),
            nutritionGrade = dto.nutritionGrade.orEmpty().uppercase(), novaGroup = dto.novaGroup,
            nutrients = nutrients
        )
    }

    private fun JsonObject?.toNutrients(): List<ProductNutrient> {
        if (this == null) return emptyList()
        return entrySet().asSequence()
            .filter { (key, value) -> key.endsWith("_100g") && value.isJsonPrimitive && value.asJsonPrimitive.isNumber }
            .mapNotNull { (key, value) ->
                val id = key.removeSuffix("_100g")
                if (id in excludedIds || id.endsWith("-prepared")) return@mapNotNull null
                val amount = runCatching { value.asDouble }.getOrNull()
                    ?.takeIf { it.isFinite() && it >= 0 } ?: return@mapNotNull null
                val unit = get("${id}_unit")?.takeIf { it.isJsonPrimitive }?.asString
                    ?.lowercase()?.replace("µ", "μ") ?: defaultUnit(id)
                ProductNutrient(id, nutrientLabel(id), amount, unit, dailyPercent(id, amount, unit))
            }
            .distinctBy { it.id }
            .sortedWith(compareByDescending<ProductNutrient> { it.dailyValuePercent ?: -1.0 }
                .thenByDescending { normalizedGrams(it.amountPer100g, it.unit) })
            .toList()
    }

    private fun List<ProductNutrient>.amount(id: String) = firstOrNull { it.id == id }?.amountPer100g ?: 0.0

    private fun dailyPercent(id: String, amount: Double, unit: String): Double? {
        val reference = dailyReferences[id] ?: return null
        val normalized = when (unit) {
            "kg" -> amount * 1_000.0
            "mg" -> amount / 1_000.0
            "μg", "ug", "mcg" -> amount / 1_000_000.0
            "kj" -> amount / 4.184
            else -> amount
        }
        return (normalized / reference * 100.0).coerceAtMost(999.0)
    }

    private fun normalizedGrams(amount: Double, unit: String) = when (unit) {
        "kg" -> amount * 1_000.0; "mg" -> amount / 1_000.0
        "μg", "ug", "mcg" -> amount / 1_000_000.0; else -> amount
    }

    private fun defaultUnit(id: String) = if (id.startsWith("energy-")) "kcal" else "g"

    private fun cleanTag(value: String) = value.substringAfter(':').replace('-', ' ')
        .replaceFirstChar { it.uppercase() }

    private fun nutrientLabel(id: String) = labels[id] ?: id.replace('-', ' ')
        .replaceFirstChar { it.uppercase() }

    private val excludedIds = setOf("energy", "energy-kj", "nutrition-score-fr", "nutrition-score-uk")
    private val labels = mapOf(
        "energy-kcal" to "Energía", "fat" to "Grasas", "saturated-fat" to "Grasas saturadas",
        "carbohydrates" to "Carbohidratos", "sugars" to "Azúcares", "fiber" to "Fibra",
        "proteins" to "Proteínas", "salt" to "Sal", "sodium" to "Sodio",
        "vitamin-a" to "Vitamina A", "vitamin-c" to "Vitamina C", "vitamin-d" to "Vitamina D",
        "vitamin-b1" to "Vitamina B1", "vitamin-b2" to "Vitamina B2", "vitamin-b6" to "Vitamina B6",
        "vitamin-b9" to "Vitamina B9", "vitamin-b12" to "Vitamina B12", "vitamin-e" to "Vitamina E",
        "calcium" to "Calcio", "iron" to "Hierro", "magnesium" to "Magnesio",
        "potassium" to "Potasio", "zinc" to "Zinc", "phosphorus" to "Fósforo"
    )
    // Values are grams/day (kcal/day for energy). Used only to order/highlight, not as medical advice.
    private val dailyReferences = mapOf(
        "energy-kcal" to 2_000.0, "fat" to 70.0, "saturated-fat" to 20.0,
        "carbohydrates" to 260.0, "sugars" to 90.0, "fiber" to 25.0, "proteins" to 50.0,
        "salt" to 6.0, "sodium" to 2.4, "vitamin-a" to .0008, "vitamin-c" to .08,
        "vitamin-d" to .000005, "vitamin-b1" to .0011, "vitamin-b2" to .0014,
        "vitamin-b6" to .0014, "vitamin-b9" to .0002, "vitamin-b12" to .0000025,
        "vitamin-e" to .012, "calcium" to .8, "iron" to .014, "magnesium" to .375,
        "potassium" to 2.0, "zinc" to .01, "phosphorus" to .7
    )
}
