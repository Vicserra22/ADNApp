package com.adn.adnapp.domain.model

import java.util.UUID

enum class AgendaItemKind(val label: String) {
    TASK("Tarea"), EVENT("Evento"), REMINDER("Recordatorio"), HABIT("Hábito")
}

data class AgendaItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val note: String = "",
    val date: String? = null,
    val time: String = "",
    val kind: AgendaItemKind = AgendaItemKind.TASK,
    val completed: Boolean = false,
    val projectId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class AgendaProject(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val goal: String = "",
    val deadline: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class AgendaData(
    val items: List<AgendaItem> = emptyList(),
    val projects: List<AgendaProject> = emptyList()
)
