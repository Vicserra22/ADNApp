package com.adn.adnapp.domain.model

import java.util.UUID

enum class MoneyKind(val label: String) { EXPENSE("Gasto"), INCOME("Ingreso") }

data class MoneyEntry(
    val id: String = UUID.randomUUID().toString(), val title: String, val amountCents: Long,
    val date: String, val kind: MoneyKind, val category: String = "General", val planned: Boolean = false,
    val recurring: Boolean = false, val createdAt: Long = System.currentTimeMillis()
)

data class FinanceGoal(
    val id: String = UUID.randomUUID().toString(), val name: String, val targetCents: Long,
    val savedCents: Long = 0, val deadline: String? = null, val createdAt: Long = System.currentTimeMillis()
)

data class InvestmentPosition(
    val id: String = UUID.randomUUID().toString(), val name: String, val units: Double,
    val averagePriceCents: Long, val currentPriceCents: Long, val createdAt: Long = System.currentTimeMillis()
)

data class FinanceData(
    val entries: List<MoneyEntry> = emptyList(), val goals: List<FinanceGoal> = emptyList(),
    val investments: List<InvestmentPosition> = emptyList()
)
