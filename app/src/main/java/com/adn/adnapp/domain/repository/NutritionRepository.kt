package com.adn.adnapp.domain.repository

import com.adn.adnapp.data.model.entity.NutritionProfile

interface NutritionRepository {
    suspend fun getNutritionProfile(uid: String): Result<NutritionProfile?>
    suspend fun completeOnboarding(uid: String, dietId: String): Result<Unit>
    suspend fun updateDiet(uid: String, dietId: String): Result<Unit>
}
