package com.adn.adnapp.data.repository

import com.adn.adnapp.data.model.entity.UserProfile
import com.adn.adnapp.data.remote.firebase.FirestoreDataSource
import com.adn.adnapp.domain.repository.UserRepository

class UserRepositoryImpl(
    private val firestoreDataSource: FirestoreDataSource
) : UserRepository {

    override suspend fun saveUserProfile(uid: String, profile: UserProfile): Result<Unit> {
        return try {
            firestoreDataSource.saveUserProfile(uid, profile)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserProfile(uid: String): Result<UserProfile?> {
        return try {
            val profile = firestoreDataSource.getUserProfile(uid)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateField(uid: String, field: String, value: Any): Result<Unit> {
        return try {
            firestoreDataSource.updateField(uid, field, value)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun userExists(uid: String): Result<Boolean> {
        return try {
            val profile = firestoreDataSource.getUserProfile(uid)
            Result.success(profile != null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
