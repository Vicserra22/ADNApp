package com.adn.adnapp.data.model.entity

import com.adn.adnapp.domain.model.BodyGoal
import com.adn.adnapp.domain.model.Importance

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val age: Int = 0,
    val weight: Double = 0.0,
    val height: Double = 0.0,
    val gender: String = "",
    val targetWeight: Double = 0.0,
    val bodyGoal: BodyGoal = BodyGoal.MAINTAIN,
    val nutritionImportance: Importance = Importance.NORMAL,
    val sportsImportance: Importance = Importance.NORMAL,
    val goalsImportance: Importance = Importance.NORMAL,
    val prioritiesCompleted: Boolean = false
)
