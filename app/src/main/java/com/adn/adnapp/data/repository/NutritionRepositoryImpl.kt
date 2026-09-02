package com.adn.adnapp.data.repository

import com.adn.adnapp.data.model.entity.NutritionProfile
import com.adn.adnapp.data.remote.firebase.FirestoreDataSource
import com.adn.adnapp.domain.repository.NutritionRepository

class NutritionRepositoryImpl(
    private val firestoreDataSource: FirestoreDataSource
) : NutritionRepository {
    override suspend fun getNutritionProfile(uid: String): Result<NutritionProfile?> = runCatching {
        firestoreDataSource.getNutritionProfile(uid)
    }

    override suspend fun completeOnboarding(uid: String, dietId: String): Result<Unit> = runCatching {
        firestoreDataSource.saveNutritionProfile(
            uid = uid,
            profile = NutritionProfile(dietId = dietId, onboardingCompleted = true)
        )
    }

    override suspend fun updateDiet(uid: String, dietId: String): Result<Unit> = runCatching {
        val current = firestoreDataSource.getNutritionProfile(uid)
        firestoreDataSource.saveNutritionProfile(
            uid = uid,
            profile = NutritionProfile(
                dietId = dietId,
                onboardingCompleted = current?.onboardingCompleted ?: true
            )
        )
    }
}
