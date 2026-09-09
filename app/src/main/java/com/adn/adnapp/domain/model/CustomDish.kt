package com.adn.adnapp.domain.model

import com.adn.adnapp.data.model.entity.Product
import java.util.UUID

data class DishIngredient(val product: Product, val grams: Double)

data class CustomDishDraft(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val finalWeight: String = "",
    val ingredients: List<DishIngredient> = emptyList()
)

fun CustomDishDraft.toProduct(ownerId: String): Product {
    val weight = finalWeight.replace(',', '.').toDoubleOrNull()
    require(name.isNotBlank()) { "Pon un nombre a tu plato" }
    require(weight != null && weight.isFinite() && weight > 0) { "Indica el peso final del plato en gramos" }
    require(ingredients.isNotEmpty()) { "Añade al menos un ingrediente" }
    require(ingredients.all { it.grams.isFinite() && it.grams > 0 }) { "Revisa las cantidades de los ingredientes" }
    fun amount(select: (Product) -> Double): Double {
        require(ingredients.all { select(it.product).isFinite() && select(it.product) >= 0 }) { "Un ingrediente tiene datos nutricionales inválidos" }
        return (ingredients.sumOf { select(it.product) * it.grams } / weight).also {
            require(it.isFinite()) { "Revisa el peso final y las cantidades" }
        }
    }
    return Product(
        code = "dish:$ownerId:$id", name = name.trim(), imageUrl = null,
        calories = amount { it.calories }, fats = amount { it.fats }, proteins = amount { it.proteins },
        carbs = amount { it.carbs }, sugars = amount { it.sugars },
        quantity = "$weight g", brands = "Lo mejor de la casa",
        ingredients = ingredients.joinToString("; ") { "${it.product.name}: ${it.grams} g" },
        allergens = ingredients.flatMap { it.product.allergens }.distinct(), categories = listOf("Platos propios")
    )
}
