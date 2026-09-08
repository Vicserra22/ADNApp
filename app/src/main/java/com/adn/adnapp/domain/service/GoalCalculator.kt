package com.adn.adnapp.domain.service

import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.data.model.entity.Diet
import com.adn.adnapp.data.model.entity.UserProfile
import com.adn.adnapp.domain.model.BodyGoal
import com.adn.adnapp.domain.model.DayScore
import com.adn.adnapp.domain.model.DailyActivityLevel
import com.adn.adnapp.domain.model.GoalRule
import com.adn.adnapp.domain.model.MacroTolerance
import com.adn.adnapp.domain.model.NutrientGoal
import com.adn.adnapp.domain.model.NutritionTargets

object GoalCalculator {
    fun targets(
        profile: UserProfile,
        diet: Diet,
        activityLevel: DailyActivityLevel = DailyActivityLevel.LIGHT,
        macroTolerance: MacroTolerance = MacroTolerance.NORMAL
    ): NutritionTargets {
        if (diet.id.startsWith(CUSTOM_DIET_PREFIX)) {
            return customTargets(diet, activityLevel, macroTolerance)
        }

        val sexOffset = if (profile.gender.equals("Hombre", true)) 5.0 else -161.0
        val bmr = 10 * profile.weight + 6.25 * profile.height - 5 * profile.age + sexOffset
        val goalOffset = when (profile.bodyGoal) {
            BodyGoal.LOSE_WEIGHT -> -300.0
            BodyGoal.MAINTAIN -> 0.0
            BodyGoal.GAIN_WEIGHT -> 300.0
        }
        val personalisedCalories = (bmr * activityLevel.pal + goalOffset).coerceAtLeast(MINIMUM_CALORIES)
        val calories = if (profile.weight > 0 && profile.height > 0 && profile.age > 0) personalisedCalories
            else diet.calories.coerceAtLeast(2_000.0) * activityLevel.ratioFromDefault()
        // Normalise the preset's macro proportions so their energy always adds up to the
        // personalised calorie target, even if a catalogue entry is rounded by a few kcal.
        val baseCalories = (diet.proteins * 4 + diet.carbs * 4 + diet.lipids * 9).takeIf { it > 0 }
            ?: diet.calories.takeIf { it > 0 }
            ?: calories
        val scale = calories / baseCalories
        val proteins = (diet.proteins * scale).takeIf { it > 0 } ?: calories * .25 / 4
        val fats = (diet.lipids * scale).takeIf { it > 0 } ?: calories * .28 / 9
        val carbs = (diet.carbs * scale).takeIf { it > 0 }
            ?: ((calories - proteins * 4 - fats * 9) / 4).coerceAtLeast(0.0)
        val sugarMax = (diet.sugar * scale).takeIf { it > 0 } ?: calories * .10 / 4
        val water = diet.water.takeIf { it > 0 }
            ?: (profile.weight * 30).takeIf { it > 0 }
            ?: 2_000.0
        val lowCarb = diet.id == LOW_CARB_DIET_ID
        val margins = macroTolerance.margins()
        val carbMaximum = carbs * if (lowCarb) margins.lowCarbAccepted else margins.macroAccepted
        val carbCritical = carbs * if (lowCarb) margins.lowCarbCritical else margins.macroCritical
        return NutritionTargets(
            calories = calories,
            proteins = proteins,
            carbs = carbs,
            fats = fats,
            sugarMax = sugarMax,
            waterMl = water,
            caloriesGoal = NutrientGoal.range(
                calories * .90, calories, calories * margins.calorieAccepted,
                calories * margins.calorieCritical
            ),
            proteinGoal = NutrientGoal.minimum(proteins * .90, proteins),
            carbsGoal = NutrientGoal.range(
                carbs * if (lowCarb) .75 else .80, carbs, carbMaximum, carbCritical
            ),
            fatsGoal = NutrientGoal.range(
                fats * .80, fats, fats * margins.macroAccepted, fats * margins.macroCritical
            ),
            sugarGoal = NutrientGoal.maximum(sugarMax),
            waterGoal = NutrientGoal.minimum(water, water)
        )
    }

    fun score(consumption: DailyConsumption, targets: NutritionTargets, profile: UserProfile): DayScore {
        val calories = goalScore(consumption.calories, targets.caloriesGoal)
        val proteins = goalScore(consumption.proteins, targets.proteinGoal)
        val carbs = goalScore(consumption.carbs, targets.carbsGoal)
        val fats = goalScore(consumption.fats, targets.fatsGoal)
        val sugar = goalScore(consumption.sugar, targets.sugarGoal)
        val water = goalScore(consumption.waterMl, targets.waterGoal)
        val nutrition = listOf(carbs, fats, sugar, water).average()
        val objectives = listOf(calories, proteins).average()
        val nutritionWeight = profile.nutritionImportance.weight.toDouble()
        val objectivesWeight = profile.goalsImportance.weight.toDouble()
        val hasActivity = consumption.calories > 0 || consumption.proteins > 0 ||
            consumption.carbs > 0 || consumption.fats > 0 || consumption.waterMl > 0
        val total = if (!hasActivity) 0.0 else
            (nutrition * nutritionWeight + objectives * objectivesWeight) /
                (nutritionWeight + objectivesWeight)
        return DayScore(total, calories, proteins, carbs, fats, sugar, water)
    }

    private fun customTargets(
        diet: Diet,
        activityLevel: DailyActivityLevel,
        macroTolerance: MacroTolerance
    ): NutritionTargets {
        // The user's custom values are the LIGHT-day baseline. Other selections are
        // explicit, reversible daily estimates rather than changes to the saved diet.
        val activityScale = activityLevel.ratioFromDefault()
        val calories = diet.calories.coerceAtLeast(0.0) * activityScale
        val proteins = diet.proteins.coerceAtLeast(0.0) * activityScale
        val carbs = diet.carbs.coerceAtLeast(0.0) * activityScale
        val fats = diet.lipids.coerceAtLeast(0.0) * activityScale
        val sugar = (diet.sugar.takeIf { it > 0 } ?: diet.calories * .10 / 4) * activityScale
        val water = (diet.water.takeIf { it > 0 } ?: 2_000.0) * when (activityLevel) {
            DailyActivityLevel.ACTIVE -> 1.10
            DailyActivityLevel.VERY_ACTIVE -> 1.20
            else -> 1.0
        }
        val margins = macroTolerance.margins()
        return NutritionTargets(
            calories = calories,
            proteins = proteins,
            carbs = carbs,
            fats = fats,
            sugarMax = sugar,
            waterMl = water,
            caloriesGoal = NutrientGoal.range(
                calories * .90, calories, calories * margins.calorieAccepted,
                calories * margins.calorieCritical
            ),
            proteinGoal = NutrientGoal.minimum(proteins * .90, proteins),
            carbsGoal = NutrientGoal.range(
                carbs * .80, carbs, carbs * margins.macroAccepted, carbs * margins.macroCritical
            ),
            fatsGoal = NutrientGoal.range(
                fats * .80, fats, fats * margins.macroAccepted, fats * margins.macroCritical
            ),
            sugarGoal = NutrientGoal.maximum(sugar),
            waterGoal = NutrientGoal.minimum(water, water)
        )
    }

    private fun goalScore(actual: Double, goal: NutrientGoal): Double {
        if (goal.target <= 0) return 1.0
        return when (goal.rule) {
            GoalRule.MAXIMUM -> upperScore(actual, goal.maximum ?: goal.target)
            GoalRule.MINIMUM -> {
                val minimum = goal.minimum ?: goal.target
                when {
                    actual < minimum -> (actual / minimum).coerceIn(0.0, 1.0)
                    goal.maximum != null && actual > goal.maximum -> upperScore(actual, goal.maximum)
                    else -> 1.0
                }
            }
            GoalRule.RANGE -> {
                val minimum = goal.minimum ?: goal.target
                val maximum = goal.maximum ?: goal.target
                when {
                    actual < minimum -> (actual / minimum).coerceIn(0.0, 1.0)
                    actual > maximum -> upperScore(actual, maximum)
                    else -> 1.0
                }
            }
        }
    }

    private fun upperScore(actual: Double, maximum: Double): Double = when {
        maximum <= 0 -> 1.0
        actual <= maximum -> 1.0
        else -> (1.0 - (actual - maximum) / maximum).coerceIn(0.0, 1.0)
    }

    private const val CUSTOM_DIET_PREFIX = "custom_"
    private const val LOW_CARB_DIET_ID = "low_carb"
    private const val MINIMUM_CALORIES = 1_200.0
}

private fun DailyActivityLevel.ratioFromDefault() = pal / DailyActivityLevel.LIGHT.pal

private data class ToleranceMargins(
    val calorieAccepted: Double,
    val calorieCritical: Double,
    val macroAccepted: Double,
    val macroCritical: Double,
    val lowCarbAccepted: Double,
    val lowCarbCritical: Double
)

private fun MacroTolerance.margins() = when (this) {
    MacroTolerance.PERMISSIVE -> ToleranceMargins(1.15, 1.35, 1.30, 1.50, 1.15, 1.30)
    MacroTolerance.NORMAL -> ToleranceMargins(1.10, 1.25, 1.20, 1.40, 1.10, 1.25)
    MacroTolerance.STRICT -> ToleranceMargins(1.05, 1.15, 1.10, 1.20, 1.05, 1.15)
}
