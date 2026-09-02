package com.adn.adnapp.domain.service

import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.data.model.entity.Diet
import com.adn.adnapp.data.model.entity.UserProfile
import com.adn.adnapp.domain.model.BodyGoal
import com.adn.adnapp.domain.model.DayScore
import com.adn.adnapp.domain.model.NutritionTargets
import kotlin.math.abs
import kotlin.math.max

object GoalCalculator {
    fun targets(profile: UserProfile, diet: Diet): NutritionTargets {
        val sexOffset = if (profile.gender.equals("Hombre", true)) 5.0 else -161.0
        val bmr = 10 * profile.weight + 6.25 * profile.height - 5 * profile.age + sexOffset
        val goalOffset = when (profile.bodyGoal) {
            BodyGoal.LOSE_WEIGHT -> -300.0
            BodyGoal.MAINTAIN -> 0.0
            BodyGoal.GAIN_WEIGHT -> 300.0
        }
        val personalisedCalories = (bmr * 1.35 + goalOffset).coerceAtLeast(1_200.0)
        val calories = if (profile.weight > 0 && profile.height > 0 && profile.age > 0) personalisedCalories
            else diet.calories.coerceAtLeast(2_000.0)
        val proteinFactor = when (profile.bodyGoal) {
            BodyGoal.LOSE_WEIGHT -> 1.8
            BodyGoal.MAINTAIN -> 1.5
            BodyGoal.GAIN_WEIGHT -> 1.7
        }
        val proteins = max(diet.proteins, profile.weight * proteinFactor)
        val fats = if (diet.lipids > 0) diet.lipids else calories * .28 / 9
        val carbs = if (diet.carbs > 0) diet.carbs else (calories - proteins * 4 - fats * 9) / 4
        return NutritionTargets(
            calories = calories, proteins = proteins, carbs = carbs.coerceAtLeast(0.0), fats = fats,
            sugarMax = diet.sugar.takeIf { it > 0 } ?: 50.0,
            waterMl = diet.water.takeIf { it > 0 } ?: 2_000.0
        )
    }

    fun score(consumption: DailyConsumption, targets: NutritionTargets, profile: UserProfile): DayScore {
        val calories = closeness(consumption.calories, targets.calories)
        val proteins = minimumGoal(consumption.proteins, targets.proteins)
        val carbs = closeness(consumption.carbs, targets.carbs)
        val fats = closeness(consumption.fats, targets.fats)
        val sugar = maximumGoal(consumption.sugar, targets.sugarMax)
        val water = minimumGoal(consumption.waterMl, targets.waterMl)
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

    private fun closeness(actual: Double, target: Double): Double {
        if (target <= 0 || actual <= 0) return 0.0
        return (1.0 - abs(actual - target) / target).coerceIn(0.0, 1.0)
    }

    private fun minimumGoal(actual: Double, target: Double): Double =
        if (target <= 0) 1.0 else (actual / target).coerceIn(0.0, 1.0)

    private fun maximumGoal(actual: Double, maximum: Double): Double = when {
        maximum <= 0 -> 1.0
        actual <= maximum -> 1.0
        else -> (1.0 - (actual - maximum) / maximum).coerceIn(0.0, 1.0)
    }
}
