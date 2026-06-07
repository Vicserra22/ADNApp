package com.adn.adnapp.data.mapper

import com.adn.adnapp.data.model.dto.ProductDto
import com.adn.adnapp.data.model.entity.Product

object ProductMapper {
    fun mapToDomain(dto: ProductDto): Product {
        return Product(
            code = dto.code ?: "",
            name = dto.name ?: "Unknown",
            imageUrl = dto.imageUrl,
            calories = dto.nutriments?.calories ?: 0.0,
            fats = dto.nutriments?.fat ?: 0.0,
            proteins = dto.nutriments?.proteins ?: 0.0,
            carbs = dto.nutriments?.carbs ?: 0.0,
            sugars = dto.nutriments?.sugars ?: 0.0
        )
    }
}
