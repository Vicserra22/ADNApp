package com.adn.adnapp.data.mapper

import com.adn.adnapp.data.model.dto.SearchFoodResponse
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchFoodMapperTest {
    @Test
    fun realSearchALiciousShape_mapsSpanishNameBrandAndMacros() {
        val json = """{
          "hits": [{
            "code": "8433329104708",
            "product_name": "Yogurt",
            "product_name_es": "Yogur con fresas",
            "brands": ["El Corte Inglés"],
            "image_front_small_url": "https://images.example/yogur.jpg",
            "nutrition_grades": "c",
            "nova_group": 4,
            "nutriments": {
              "energy-kcal_100g": 96,
              "proteins_100g": 4.2,
              "carbohydrates_100g": 12.1,
              "fat_100g": 3.1,
              "sugars_100g": 11.6
            }
          }]
        }"""

        val hit = Gson().fromJson(json, SearchFoodResponse::class.java).hits.single()
        val product = ProductMapper.mapToDomain(hit)

        assertEquals("Yogur con fresas", product.name)
        assertEquals("El Corte Inglés", product.brands)
        assertEquals(96.0, product.calories, 0.0)
        assertEquals(4.2, product.proteins, 0.0)
        assertEquals("C", product.nutritionGrade)
    }
}
