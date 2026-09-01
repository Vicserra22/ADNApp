package com.adn.adnapp.domain.model

enum class AppArea(val id: String, val isAvailable: Boolean) {
    NUTRITION("nutrition", true),
    SPORTS("sports", false),
    FINANCE("finance", false),
    PHILOSOPHY("philosophy", false);

    companion object {
        fun fromId(id: String): AppArea? = entries.firstOrNull { it.id == id }
    }
}
