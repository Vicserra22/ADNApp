package com.adn.adnapp.data.repository

import com.adn.adnapp.data.remote.firebase.AuthDataSource
import com.adn.adnapp.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authDataSource: AuthDataSource
) : AuthRepository {

    override fun isAuthenticated(): Boolean {
        return authDataSource.currentUser != null
    }

    override fun getCurrentUserId(): String? {
        return authDataSource.currentUser?.uid
    }

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            authDataSource.login(email, password)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String): Result<Unit> {
        return try {
            authDataSource.register(email, password)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun logout() {
        authDataSource.logout()
    }
}
