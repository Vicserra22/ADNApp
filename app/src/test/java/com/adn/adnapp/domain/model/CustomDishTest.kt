package com.adn.adnapp.domain.model

import com.adn.adnapp.data.model.entity.Product
import org.junit.Assert.*
import org.junit.Test

class CustomDishTest {
    private val rice = Product("rice", "Arroz seco", null, 360.0, 1.0, 7.0, 80.0, .5)
    private val chicken = Product("chicken", "Pollo", null, 120.0, 2.0, 23.0, 0.0, 0.0)

    @Test fun usesCookedWeightInsteadOfRawIngredientWeight() {
        val dish = CustomDishDraft(name = "Arroz con pollo", finalWeight = "600",
            ingredients = listOf(DishIngredient(rice, 100.0), DishIngredient(chicken, 200.0))).toProduct("user")
        assertEquals(100.0, dish.calories, .001)
        assertEquals(53.0 / 6, dish.proteins, .001)
        assertEquals(80.0 / 6, dish.carbs, .001)
    }

    @Test fun maintainsIdentityAndOwnerWhenSavingAgain() {
        val draft = CustomDishDraft(name = "Ensalada", finalWeight = "100,5", ingredients = listOf(DishIngredient(rice, 100.0)))
        assertEquals(draft.toProduct("a").code, draft.copy(name = "Mi ensalada").toProduct("a").code)
        assertNotEquals(draft.toProduct("a").code, draft.toProduct("b").code)
    }

    @Test fun rejectsInvalidAndMissingQuantities() {
        val draft = CustomDishDraft(name = "Plato", finalWeight = "100", ingredients = listOf(DishIngredient(rice, 100.0)))
        listOf("", "0", "-10", "NaN", "Infinity").forEach { weight ->
            assertTrue(runCatching { draft.copy(finalWeight = weight).toProduct("a") }.isFailure)
        }
        assertTrue(runCatching { draft.copy(ingredients = emptyList()).toProduct("a") }.isFailure)
        assertTrue(runCatching { draft.copy(ingredients = listOf(DishIngredient(rice, Double.NaN))).toProduct("a") }.isFailure)
    }

    @Test fun retainsDeclaredAllergensWithoutInventingMicronutrients() {
        val dish = CustomDishDraft(name = "Plato", finalWeight = "100",
            ingredients = listOf(DishIngredient(rice.copy(allergens = listOf("gluten")), 100.0))).toProduct("a")
        assertEquals(listOf("gluten"), dish.allergens)
        assertTrue(dish.nutrients.isEmpty())
    }
}
