package com.adn.adnapp.domain.repository

import com.adn.adnapp.data.model.entity.Diet

interface DietRepository {
    suspend fun getAvailableDiets(userId: String? = null): Result<List<Diet>>
    suspend fun getDiet(dietId: String, userId: String? = null): Result<Diet?>
    suspend fun saveCustomDiet(userId: String, diet: Diet): Result<Unit>
}
