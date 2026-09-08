package com.adn.adnapp.data.model.entity

import com.adn.adnapp.domain.model.DailyActivityLevel

data class DailyConsumption(
    val date: String = "",
    val calories: Double = 0.0,
    val proteins: Double = 0.0,
    val carbs: Double = 0.0,
    val fats: Double = 0.0,
    val sugar: Double = 0.0,
    val waterMl: Double = 0.0,
    val activityLevel: DailyActivityLevel = DailyActivityLevel.LIGHT
)
