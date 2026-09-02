package com.adn.adnapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun isAuthenticated(): Boolean
    fun getCurrentUserId(): String?
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String): Result<Unit>
    fun logout()
}
