package com.adn.adnapp.domain.service

import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.data.model.entity.Diet
import com.adn.adnapp.data.model.entity.UserProfile
import com.adn.adnapp.domain.model.BodyGoal
import com.adn.adnapp.domain.model.GoalRule
import com.adn.adnapp.domain.model.Importance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalCalculatorTest {
    private val profile = UserProfile(
        age = 30, weight = 75.0, height = 178.0, gender = "Hombre",
        bodyGoal = BodyGoal.MAINTAIN, nutritionImportance = Importance.IMPORTANT,
        goalsImportance = Importance.VERY_IMPORTANT
    )
    private val balanced = Diet(
        id = "balanced", calories = 2_100.0, proteins = 120.0, carbs = 250.0,
        lipids = 70.0, sugar = 50.0, water = 2_000.0
    )

    @Test
    fun customDiet_keepsEveryUserMacroExactly_andIgnoresAnthropometry() {
        val custom = Diet(
            id = "custom_cut", calories = 1_845.0, proteins = 142.0,
            carbs = 173.0, lipids = 65.0, sugar = 36.0, water = 2_450.0
        )
        val veryDifferentProfile = profile.copy(age = 70, weight = 130.0, height = 155.0)

        val first = GoalCalculator.targets(profile, custom)
        val second = GoalCalculator.targets(veryDifferentProfile, custom)

        assertEquals(1_845.0, first.calories, 0.0)
        assertEquals(142.0, first.proteins, 0.0)
        assertEquals(173.0, first.carbs, 0.0)
        assertEquals(65.0, first.fats, 0.0)
        assertEquals(first, second)
    }

    @Test
    fun preset_usesMifflinAndScalesDietCompositionToPersonalCalories() {
        val targets = GoalCalculator.targets(profile, balanced)
        val expectedCalories = (10 * 75.0 + 6.25 * 178.0 - 5 * 30 + 5) * 1.35
        val presetMacroCalories = balanced.proteins * 4 + balanced.carbs * 4 + balanced.lipids * 9
        val scale = expectedCalories / presetMacroCalories

        assertEquals(expectedCalories, targets.calories, 0.001)
        assertEquals(balanced.proteins * scale, targets.proteins, 0.001)
        assertEquals(balanced.carbs * scale, targets.carbs, 0.001)
        assertEquals(balanced.lipids * scale, targets.fats, 0.001)
        assertEquals(expectedCalories,
            targets.proteins * 4 + targets.carbs * 4 + targets.fats * 9, 0.001)
        assertEquals(GoalRule.RANGE, targets.caloriesGoal.rule)
        assertEquals(GoalRule.MINIMUM, targets.proteinGoal.rule)
        assertEquals(GoalRule.MAXIMUM, targets.sugarGoal.rule)
    }

    @Test
    fun protein_doesNotPenaliseASlightExcess() {
        val targets = GoalCalculator.targets(profile, balanced)
        val day = atTargets(targets).copy(proteins = targets.proteins * 1.10)

        assertEquals(1.0, GoalCalculator.score(day, targets, profile).proteins, 0.0)
    }

    @Test
    fun lowCarbPreset_hasAStrictCarbohydrateMaximum() {
        val lowCarb = balanced.copy(id = "low_carb", carbs = 120.0, lipids = 105.0)
        val targets = GoalCalculator.targets(profile, lowCarb)
        val atMaximum = atTargets(targets)
        val overMaximum = atMaximum.copy(carbs = targets.carbs * 1.25)

        assertEquals(targets.carbs, targets.carbsGoal.maximum!!, 0.0)
        assertEquals(1.0, GoalCalculator.score(atMaximum, targets, profile).carbs, 0.0)
        assertTrue(GoalCalculator.score(overMaximum, targets, profile).carbs < 1.0)
    }

    @Test
    fun sugarIsAMaximum_andWaterIsAMinimum() {
        val targets = GoalCalculator.targets(profile, balanced)
        val base = atTargets(targets)
        val result = GoalCalculator.score(
            base.copy(sugar = targets.sugarMax * 2, waterMl = targets.waterMl / 2),
            targets,
            profile
        )

        assertEquals(0.0, result.sugar, 0.0001)
        assertEquals(0.5, result.water, 0.0001)
    }

    @Test
    fun emptyDay_hasZeroCompliance() {
        val targets = GoalCalculator.targets(profile, balanced)
        val score = GoalCalculator.score(DailyConsumption(date = "2026-09-02"), targets, profile)
        assertEquals(0.0, score.total, 0.0)
    }

    private fun atTargets(targets: com.adn.adnapp.domain.model.NutritionTargets) = DailyConsumption(
        date = "2026-09-02", calories = targets.calories, proteins = targets.proteins,
        carbs = targets.carbs, fats = targets.fats, sugar = targets.sugarMax * .5,
        waterMl = targets.waterMl
    )
}
