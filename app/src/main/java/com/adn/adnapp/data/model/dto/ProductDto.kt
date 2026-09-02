package com.adn.adnapp.data.model.dto

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonObject

data class ProductDto(
    @SerializedName("code") val code: String?,
    @SerializedName("product_name") val name: String?,
    @SerializedName("image_front_small_url") val smallImageUrl: String?,
    @SerializedName("image_front_url") val imageUrl: String?,
    @SerializedName("brands") val brands: String?,
    @SerializedName("quantity") val quantity: String?,
    @SerializedName("serving_size") val servingSize: String?,
    @SerializedName("ingredients_text") val ingredients: String?,
    @SerializedName("allergens_tags") val allergens: List<String>?,
    @SerializedName("categories_tags") val categories: List<String>?,
    @SerializedName("nutrition_grade_fr") val nutritionGrade: String?,
    @SerializedName("nova_group") val novaGroup: Int?,
    @SerializedName("nutriments") val nutriments: JsonObject?
)
