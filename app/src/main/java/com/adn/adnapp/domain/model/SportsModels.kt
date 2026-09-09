package com.adn.adnapp.domain.model

import java.util.UUID

enum class SportKind(val label: String, val family: String) {
    RUN("Correr", "cardio"), WALK("Caminar", "cardio"), CYCLING("Ciclismo", "cardio"),
    STRENGTH("Fuerza", "strength"), SWIMMING("Natación", "cardio"), YOGA("Yoga", "mobility")
}

data class SportSession(
    val id: String = UUID.randomUUID().toString(),
    val sport: SportKind,
    val date: String,
    val minutes: Int,
    val distanceKm: Double = 0.0,
    val sets: Int = 0,
    val reps: Int = 0,
    val weightKg: Double = 0.0,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class SportsData(val sessions: List<SportSession> = emptyList())
