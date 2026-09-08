package com.adn.adnapp.data.model.entity

import com.adn.adnapp.domain.model.MacroTolerance

data class NutritionProfile(
    val dietId: String = "",
    val onboardingCompleted: Boolean = false,
    val macroTolerance: MacroTolerance = MacroTolerance.NORMAL
)
