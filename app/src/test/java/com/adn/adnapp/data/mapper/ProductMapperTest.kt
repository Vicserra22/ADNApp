package com.adn.adnapp.data.mapper

import com.adn.adnapp.data.model.dto.ProductDto
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductMapperTest {
    @Test fun mapsAndRanksDynamicMicronutrientsByReferenceIntake() {
        val nutriments = JsonParser.parseString(
            """{
                "energy-kcal_100g": 80, "energy-kcal_unit": "kcal",
                "fat_100g": 1.0, "fat_unit": "g",
                "proteins_100g": 3.5, "proteins_unit": "g",
                "carbohydrates_100g": 8.0, "carbohydrates_unit": "g",
                "vitamin-c_100g": 80, "vitamin-c_unit": "mg",
                "zinc_100g": 2, "zinc_unit": "mg"
            }"""
        ).asJsonObject
        val product = ProductMapper.mapToDomain(
            ProductDto(
                code = "123", name = "Yogur", smallImageUrl = "image", imageUrl = null,
                brands = "Marca", quantity = "125 g", servingSize = "125 g", ingredients = "Leche",
                allergens = listOf("es:leche"), categories = listOf("es:yogures"),
                nutritionGrade = "a", novaGroup = 1, nutriments = nutriments
            )
        )

        assertEquals(80.0, product.calories, 0.0)
        assertEquals("Vitamina C", product.nutrients.first().name)
        assertEquals(100.0, product.nutrients.first().dailyValuePercent ?: 0.0, 0.01)
        assertTrue(product.allergens.contains("Leche"))
    }
}
