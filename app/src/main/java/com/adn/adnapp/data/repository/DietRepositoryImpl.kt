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
        // Existing accounts may contain an identifier from an older catalogue.
        // Keep their goals usable until they choose one of the current diets in Profile.
        val fallback = DefaultDiets.values.firstOrNull { it.id == dietId }
            ?: DefaultDiets.values.first()
        return try {
            val remote = firestoreDataSource.getDiet(dietId)
            Result.success(remote?.withFallback(fallback) ?: fallback)
        } catch (_: Exception) {
            Result.success(fallback)
        }
    }
}

private fun Diet.withFallback(fallback: Diet?): Diet = copy(
    description = description.ifBlank { fallback?.description.orEmpty() },
    imageUrl = imageUrl.ifBlank { fallback?.imageUrl.orEmpty() },
    proteins = proteins.takeIf { it > 0 } ?: fallback?.proteins ?: 0.0,
    carbs = carbs.takeIf { it > 0 } ?: fallback?.carbs ?: 0.0,
    lipids = lipids.takeIf { it > 0 } ?: fallback?.lipids ?: 0.0,
    calories = calories.takeIf { it > 0 } ?: fallback?.calories ?: 0.0,
    sugar = sugar.takeIf { it > 0 } ?: fallback?.sugar ?: 0.0,
    water = water.takeIf { it > 0 } ?: fallback?.water ?: 0.0
)
