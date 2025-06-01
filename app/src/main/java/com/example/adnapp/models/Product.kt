package com.example.adnapp.models

import com.google.gson.annotations.SerializedName

data class Product(

    val code: String,

    @SerializedName("product_name")
    val name: String?,

    @SerializedName("image_front_url")
    val imageUrl: String?,

    @SerializedName("nutriments")
    val nutriments: Nutriments?
)

data class Nutriments(
    @SerializedName("energy-kcal_100g")
    val calories: Double?,

    @SerializedName("fat_100g")
    val fat: Double?,

    @SerializedName("proteins_100g")
    val proteins: Double?,

    @SerializedName("carbohydrates_100g")
    val carbs: Double?,

    @SerializedName("sugars_100g")
    val sugars: Double?
)
