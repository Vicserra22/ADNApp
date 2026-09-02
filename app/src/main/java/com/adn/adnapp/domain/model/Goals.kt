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
    val waterMl: Double,
    val caloriesGoal: NutrientGoal = NutrientGoal.range(calories * .90, calories, calories * 1.10),
    val proteinGoal: NutrientGoal = NutrientGoal.minimum(
        minimum = proteins * .90,
        target = proteins,
        toleratedMaximum = proteins * 1.25
    ),
    val carbsGoal: NutrientGoal = NutrientGoal.range(carbs * .85, carbs, carbs * 1.15),
    val fatsGoal: NutrientGoal = NutrientGoal.range(fats * .80, fats, fats * 1.20),
    val sugarGoal: NutrientGoal = NutrientGoal.maximum(sugarMax),
    val waterGoal: NutrientGoal = NutrientGoal.minimum(waterMl, waterMl)
)

enum class GoalRule { RANGE, MINIMUM, MAXIMUM }

/** A target with explicit healthy limits, expressed in the nutrient's own unit. */
data class NutrientGoal(
    val minimum: Double?,
    val target: Double,
    val maximum: Double?,
    val rule: GoalRule
) {
    companion object {
        fun range(minimum: Double, target: Double, maximum: Double) =
            NutrientGoal(minimum, target, maximum, GoalRule.RANGE)

        fun minimum(minimum: Double, target: Double, toleratedMaximum: Double? = null) =
            NutrientGoal(minimum, target, toleratedMaximum, GoalRule.MINIMUM)

        fun maximum(maximum: Double) =
            NutrientGoal(null, maximum, maximum, GoalRule.MAXIMUM)
    }
}

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
