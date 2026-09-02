package com.adn.adnapp.data.repository

import com.adn.adnapp.data.model.entity.Diet
import com.adn.adnapp.data.remote.firebase.FirestoreDataSource
import com.adn.adnapp.data.source.DefaultDiets
import com.adn.adnapp.domain.repository.DietRepository

class DietRepositoryImpl(
    private val firestoreDataSource: FirestoreDataSource
) : DietRepository {

    override suspend fun getAvailableDiets(userId: String?): Result<List<Diet>> {
        val remote = runCatching { firestoreDataSource.getAvailableDiets() }.getOrDefault(emptyList())
        val custom = userId?.let {
            runCatching { firestoreDataSource.getCustomDiets(it) }.getOrDefault(emptyList())
        }.orEmpty()
        val catalogue = linkedMapOf<String, Diet>()
        DefaultDiets.values.forEach { catalogue[it.id] = it }
        remote.forEach { diet ->
            val fallback = DefaultDiets.values.firstOrNull { it.id == diet.id }
            catalogue[diet.id] = diet.withFallback(fallback)
        }
        custom.forEach { catalogue[it.id] = it }
        return Result.success(catalogue.values.toList())
    }

    override suspend fun getDiet(dietId: String, userId: String?): Result<Diet?> {
        if (dietId.startsWith("custom_") && userId != null) {
            return runCatching { firestoreDataSource.getCustomDiet(userId, dietId) }
        }
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

    override suspend fun saveCustomDiet(userId: String, diet: Diet): Result<Unit> =
        runCatching { firestoreDataSource.saveCustomDiet(userId, diet) }
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
