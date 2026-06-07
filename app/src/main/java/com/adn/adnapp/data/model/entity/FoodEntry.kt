package com.adn.adnapp.data.model.entity

data class FoodEntry(
    val productId: String = "",
    val name: String = "",
    val quantity: Double = 0.0,
    val calories: Double = 0.0,
    val proteins: Double = 0.0,
    val carbs: Double = 0.0,
    val fats: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
