package com.example.adnapp.models


data class Diet(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val proteins: Int = 0,
    val carbs: Int = 0,
    val lipids: Int = 0,
    val calories: Int = 0,
    val sugar: Int = 0,
    val water: Int = 0,
    val vitD: Int = 0
)
