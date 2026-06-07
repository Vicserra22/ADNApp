package com.adn.adnapp.data.model.entity

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val age: Int = 0,
    val weight: Double = 0.0,
    val height: Double = 0.0,
    val gender: String = "",
    val dietId: String = ""
)
