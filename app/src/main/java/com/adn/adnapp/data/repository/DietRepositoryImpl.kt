package com.adn.adnapp.data.repository

import com.adn.adnapp.data.model.entity.Diet
import com.adn.adnapp.data.remote.firebase.FirestoreDataSource
import com.adn.adnapp.domain.repository.DietRepository

class DietRepositoryImpl(
    private val firestoreDataSource: FirestoreDataSource
) : DietRepository {

    override suspend fun getAvailableDiets(): Result<List<Diet>> {
        return try {
            val diets = firestoreDataSource.getAvailableDiets()
            Result.success(diets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDiet(dietId: String): Result<Diet?> {
        return try {
            val diet = firestoreDataSource.getDiet(dietId)
            Result.success(diet)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
