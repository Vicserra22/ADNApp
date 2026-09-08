package com.adn.adnapp.domain.repository

import com.adn.adnapp.data.model.entity.NutritionProfile
import com.adn.adnapp.domain.model.MacroTolerance

interface NutritionRepository {
    suspend fun getNutritionProfile(uid: String): Result<NutritionProfile?>
    suspend fun completeOnboarding(uid: String, dietId: String, tolerance: MacroTolerance): Result<Unit>
    suspend fun updateDiet(uid: String, dietId: String, tolerance: MacroTolerance): Result<Unit>
}
