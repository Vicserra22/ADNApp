package com.adn.adnapp.data.model.entity

data class Product(
    val code: String,
    val name: String,
    val imageUrl: String?,
    val calories: Double,
    val fats: Double,
    val proteins: Double,
    val carbs: Double,
    val sugars: Double,
    val brands: String = "",
    val quantity: String = "",
    val servingSize: String = "",
    val ingredients: String = "",
    val allergens: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val nutritionGrade: String = "",
    val novaGroup: Int? = null,
    val nutrients: List<ProductNutrient> = emptyList()
)

data class ProductNutrient(
    val id: String,
    val name: String,
    val amountPer100g: Double,
    val unit: String,
    /** Percentage of an adult reference intake, when a reliable reference exists. */
    val dailyValuePercent: Double? = null
)
