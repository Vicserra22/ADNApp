package com.adn.adnapp.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthDataSource(private val auth: FirebaseAuth) {
    
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun login(email: String, password: String): FirebaseUser {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user ?: throw Exception("User null after login")
    }

    suspend fun register(email: String, password: String): FirebaseUser {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user ?: throw Exception("User null after registration")
    }

    fun logout() {
        auth.signOut()
    }
}
