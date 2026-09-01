package com.adn.adnapp.data.repository

import com.adn.adnapp.data.model.entity.Diet
import com.adn.adnapp.data.remote.firebase.FirestoreDataSource
import com.adn.adnapp.data.source.DefaultDiets
import com.adn.adnapp.domain.repository.DietRepository

class DietRepositoryImpl(
    private val firestoreDataSource: FirestoreDataSource
) : DietRepository {

    override suspend fun getAvailableDiets(): Result<List<Diet>> {
        return try {
            val diets = firestoreDataSource.getAvailableDiets()
            Result.success(diets.ifEmpty { DefaultDiets.values })
        } catch (_: Exception) {
            Result.success(DefaultDiets.values)
        }
    }

    override suspend fun getDiet(dietId: String): Result<Diet?> {
        return try {
            val diet = firestoreDataSource.getDiet(dietId)
            Result.success(diet)
        } catch (_: Exception) {
            Result.success(DefaultDiets.values.firstOrNull { it.id == dietId })
        }
    }
}
