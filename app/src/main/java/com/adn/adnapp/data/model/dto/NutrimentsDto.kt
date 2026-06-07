package com.adn.adnapp.data.model.dto

import com.google.gson.annotations.SerializedName

data class NutrimentsDto(
    @SerializedName("energy-kcal_100g") val calories: Double?,
    @SerializedName("fat_100g") val fat: Double?,
    @SerializedName("proteins_100g") val proteins: Double?,
    @SerializedName("carbohydrates_100g") val carbs: Double?,
    @SerializedName("sugars_100g") val sugars: Double?
)
