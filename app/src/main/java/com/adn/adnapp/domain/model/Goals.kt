package com.adn.adnapp.domain.model

enum class BodyGoal { LOSE_WEIGHT, MAINTAIN, GAIN_WEIGHT }

enum class Importance(val weight: Int) {
    VERY_LOW(1), LOW(2), NORMAL(3), IMPORTANT(4), VERY_IMPORTANT(5)
}

data class NutritionTargets(
    val calories: Double,
    val proteins: Double,
    val carbs: Double,
    val fats: Double,
    val sugarMax: Double,
    val waterMl: Double
)

data class DayScore(
    val total: Double,
    val calories: Double,
    val proteins: Double,
    val carbs: Double,
    val fats: Double,
    val sugar: Double,
    val water: Double
) {
    val percent: Int get() = (total * 100).toInt().coerceIn(0, 100)
}
