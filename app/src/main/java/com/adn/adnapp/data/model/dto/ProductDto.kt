package com.adn.adnapp.data.model.dto

import com.google.gson.annotations.SerializedName

data class ProductDto(
    @SerializedName("code") val code: String?,
    @SerializedName("product_name") val name: String?,
    @SerializedName("image_front_url") val imageUrl: String?,
    @SerializedName("nutriments") val nutriments: NutrimentsDto?
)
