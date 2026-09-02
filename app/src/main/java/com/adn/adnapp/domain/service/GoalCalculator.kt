package com.adn.adnapp.domain.service

import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.data.model.entity.Diet
import com.adn.adnapp.data.model.entity.UserProfile
import com.adn.adnapp.domain.model.BodyGoal
import com.adn.adnapp.domain.model.DayScore
import com.adn.adnapp.domain.model.GoalRule
import com.adn.adnapp.domain.model.NutrientGoal
import com.adn.adnapp.domain.model.NutritionTargets

object GoalCalculator {
    fun targets(profile: UserProfile, diet: Diet): NutritionTargets {
        if (diet.id.startsWith(CUSTOM_DIET_PREFIX)) return customTargets(diet)

        val sexOffset = if (profile.gender.equals("Hombre", true)) 5.0 else -161.0
        val bmr = 10 * profile.weight + 6.25 * profile.height - 5 * profile.age + sexOffset
        val goalOffset = when (profile.bodyGoal) {
            BodyGoal.LOSE_WEIGHT -> -300.0
            BodyGoal.MAINTAIN -> 0.0
            BodyGoal.GAIN_WEIGHT -> 300.0
        }
        val personalisedCalories = (bmr * ACTIVITY_FACTOR + goalOffset).coerceAtLeast(MINIMUM_CALORIES)
        val calories = if (profile.weight > 0 && profile.height > 0 && profile.age > 0) personalisedCalories
            else diet.calories.coerceAtLeast(2_000.0)
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
        return NutritionTargets(
            calories = calories,
            proteins = proteins,
            carbs = carbs,
            fats = fats,
            sugarMax = sugarMax,
            waterMl = water,
            caloriesGoal = NutrientGoal.range(calories * .90, calories, calories * 1.10),
            proteinGoal = NutrientGoal.minimum(proteins * .90, proteins, proteins * 1.25),
            carbsGoal = if (lowCarb) NutrientGoal.range(carbs * .75, carbs, carbs)
                else NutrientGoal.range(carbs * .85, carbs, carbs * 1.15),
            fatsGoal = NutrientGoal.range(fats * .80, fats, fats * 1.20),
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

    private fun customTargets(diet: Diet): NutritionTargets {
        val calories = diet.calories.coerceAtLeast(0.0)
        val proteins = diet.proteins.coerceAtLeast(0.0)
        val carbs = diet.carbs.coerceAtLeast(0.0)
        val fats = diet.lipids.coerceAtLeast(0.0)
        val sugar = diet.sugar.takeIf { it > 0 } ?: calories * .10 / 4
        val water = diet.water.takeIf { it > 0 } ?: 2_000.0
        return NutritionTargets(
            calories = calories,
            proteins = proteins,
            carbs = carbs,
            fats = fats,
            sugarMax = sugar,
            waterMl = water,
            caloriesGoal = NutrientGoal.range(calories * .90, calories, calories * 1.10),
            proteinGoal = NutrientGoal.minimum(proteins * .90, proteins, proteins * 1.25),
            carbsGoal = NutrientGoal.range(carbs * .85, carbs, carbs * 1.15),
            fatsGoal = NutrientGoal.range(fats * .80, fats, fats * 1.20),
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
    private const val ACTIVITY_FACTOR = 1.35
    private const val MINIMUM_CALORIES = 1_200.0
}
