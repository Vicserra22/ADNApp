package com.adn.adnapp.data.model.entity

data class Product(
    val code: String,
    val name: String,
    val imageUrl: String?,
    val calories: Double,
    val fats: Double,
    val proteins: Double,
    val carbs: Double,
    val sugars: Double
)
