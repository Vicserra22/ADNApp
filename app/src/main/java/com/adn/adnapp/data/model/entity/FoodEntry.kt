package com.adn.adnapp.data.model.entity

data class FoodEntry(
    val id: String = "",
    val productId: String = "",
    val name: String = "",
    val quantity: Double = 0.0,
    val calories: Double = 0.0,
    val proteins: Double = 0.0,
    val carbs: Double = 0.0,
    val fats: Double = 0.0,
    val sugar: Double = 0.0,
    val waterMl: Double = 0.0,
    val kind: String = KIND_FOOD,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val KIND_FOOD = "food"
        const val KIND_MANUAL = "manual"
        const val KIND_WATER = "water"
    }
}
