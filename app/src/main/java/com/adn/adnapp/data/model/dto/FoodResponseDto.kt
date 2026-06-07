package com.adn.adnapp.data.model.dto

import com.google.gson.annotations.SerializedName

data class FoodResponseDto(
    @SerializedName("products") val products: List<ProductDto>?
)
