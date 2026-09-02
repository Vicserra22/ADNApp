package com.adn.adnapp.data.model.entity

data class Diet(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val proteins: Double = 0.0,
    val carbs: Double = 0.0,
    val lipids: Double = 0.0,
    val calories: Double = 0.0,
    val sugar: Double = 0.0,
    val water: Double = 0.0,
    val vitD: Double = 0.0
)
