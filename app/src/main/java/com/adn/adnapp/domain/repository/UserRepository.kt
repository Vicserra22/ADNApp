package com.adn.adnapp.domain.repository

import com.adn.adnapp.data.model.entity.UserProfile

interface UserRepository {
    suspend fun saveUserProfile(uid: String, profile: UserProfile): Result<Unit>
    suspend fun getUserProfile(uid: String): Result<UserProfile?>
    suspend fun updateField(uid: String, field: String, value: Any): Result<Unit>
    suspend fun userExists(uid: String): Result<Boolean>
}
