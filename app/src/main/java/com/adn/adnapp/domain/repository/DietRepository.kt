package com.adn.adnapp.domain.repository

import com.adn.adnapp.data.model.entity.Diet

interface DietRepository {
    suspend fun getAvailableDiets(): Result<List<Diet>>
    suspend fun getDiet(dietId: String): Result<Diet?>
}
