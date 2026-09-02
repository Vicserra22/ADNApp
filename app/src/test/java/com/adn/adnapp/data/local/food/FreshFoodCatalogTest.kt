package com.adn.adnapp.data.local.food

import com.adn.adnapp.data.model.entity.Product
import com.adn.adnapp.data.model.entity.ProductNutrient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FreshFoodCatalogTest {
    @Test fun catalog_hasAllRequiredGroupsAndValidMacros() {
        val products = FreshFoodCatalog.products
        assertTrue(products.size >= 30)
        val categories = products.flatMap { it.categories }.toSet()
        listOf("Carnes", "Pescados", "Huevos", "Lácteos", "Verduras", "Frutas",
            "Legumbres", "Cereales", "Frutos secos").forEach { required ->
            assertTrue("Falta la categoría $required", required in categories)
        }
        assertEquals(products.size, products.map { it.code }.distinct().size)
        products.forEach {
            assertTrue(it.name.isNotBlank())
            assertTrue(it.calories >= 0 && it.proteins >= 0 && it.carbs >= 0 && it.fats >= 0 && it.sugars >= 0)
            assertFalse(it.imageUrl?.isNotBlank() == true)
        }
    }

    @Test fun cacheMapper_roundTripsCompleteProduct() {
        val product = Product(
            code = "8412345678901", name = "Producto prueba", imageUrl = "https://example.test/a.jpg",
            calories = 123.0, fats = 4.0, proteins = 5.0, carbs = 20.0, sugars = 3.0,
            brands = "Marca", quantity = "250 g", servingSize = "50 g", ingredients = "Uno, dos",
            allergens = listOf("Leche"), categories = listOf("Pruebas"), nutritionGrade = "B", novaGroup = 2,
            nutrients = listOf(ProductNutrient("fiber", "Fibra", 2.5, "g", 10.0))
        )
        val entity = ProductCacheMapper.toEntity(product, FoodSource.OPEN_FOOD_FACTS, updatedAt = 42L)
        val restored = ProductCacheMapper.toDomain(entity)
        assertEquals("open_food_facts:8412345678901", entity.key)
        assertEquals("8412345678901", entity.barcode)
        assertEquals(product, restored)
    }

    @Test fun normalization_isAccentAndCaseInsensitive() {
        assertEquals("platano", ProductCacheMapper.normalize("  PLÁTANO "))
    }
}
