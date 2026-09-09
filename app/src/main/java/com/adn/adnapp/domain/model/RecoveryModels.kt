package com.adn.adnapp.domain.model

import java.util.UUID

enum class HealthSource(val label: String) { MANUAL("Manual"), HEALTH_CONNECT("Health Connect") }

data class StepLog(
    val id: String = UUID.randomUUID().toString(), val date: String, val steps: Int,
    val source: HealthSource = HealthSource.MANUAL, val updatedAt: Long = System.currentTimeMillis()
)

data class SleepLog(
    val id: String = UUID.randomUUID().toString(), val date: String, val durationMinutes: Int,
    val source: HealthSource = HealthSource.MANUAL, val updatedAt: Long = System.currentTimeMillis()
)

data class RecoveryData(val steps: List<StepLog> = emptyList(), val sleep: List<SleepLog> = emptyList())
