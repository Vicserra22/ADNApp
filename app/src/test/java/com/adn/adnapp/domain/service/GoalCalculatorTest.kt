package com.adn.adnapp.domain.service

import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.data.model.entity.Diet
import com.adn.adnapp.data.model.entity.UserProfile
import com.adn.adnapp.domain.model.BodyGoal
import com.adn.adnapp.domain.model.Importance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalCalculatorTest {

    @Test
    fun emptyDay_hasZeroCompliance() {
        val profile = UserProfile(
            age = 30, weight = 75.0, height = 178.0, gender = "Hombre"
        )
        val targets = GoalCalculator.targets(profile, diet)

        val score = GoalCalculator.score(DailyConsumption(date = "2026-09-02"), targets, profile)

        assertEquals(0.0, score.total, 0.0)
    }
    private val profile = UserProfile(
        age = 30, weight = 75.0, height = 178.0, gender = "Hombre",
        bodyGoal = BodyGoal.MAINTAIN, nutritionImportance = Importance.IMPORTANT,
        goalsImportance = Importance.VERY_IMPORTANT
    )
    private val diet = Diet(calories = 2200.0, proteins = 130.0, carbs = 260.0,
        lipids = 70.0, sugar = 50.0, water = 2000.0)

    @Test fun personalisedTargets_useProfileAndDiet() {
        val targets = GoalCalculator.targets(profile, diet)
        assertTrue(targets.calories > 1_500)
        assertTrue(targets.proteins >= 130)
        assertTrue(targets.waterMl == 2_000.0)
    }

    @Test fun dayCloseToTargets_scoresHigherThanEmptyDay() {
        val targets = GoalCalculator.targets(profile, diet)
        val good = DailyConsumption("2026-09-01", targets.calories, targets.proteins,
            targets.carbs, targets.fats, 25.0, targets.waterMl)
        val empty = DailyConsumption(date = "2026-09-02")
        assertTrue(GoalCalculator.score(good, targets, profile).total > .9)
        assertTrue(GoalCalculator.score(empty, targets, profile).total < .3)
    }
}
