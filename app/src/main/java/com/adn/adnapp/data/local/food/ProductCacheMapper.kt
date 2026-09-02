package com.adn.adnapp.data.local.food

import com.adn.adnapp.data.model.entity.Product
import com.adn.adnapp.data.model.entity.ProductNutrient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.Normalizer
import java.util.Locale

object ProductCacheMapper {
    private val gson = Gson()
    private val stringListType = object : TypeToken<List<String>>() {}.type
    private val nutrientListType = object : TypeToken<List<ProductNutrient>>() {}.type

    fun toEntity(
        product: Product,
        source: String,
        sourceId: String = product.code,
        barcode: String? = product.code.takeIf { it.all(Char::isDigit) },
        updatedAt: Long = System.currentTimeMillis()
    ) = ProductCacheEntity(
        key = key(source, sourceId),
        source = source,
        sourceId = sourceId,
        barcode = barcode,
        name = product.name,
        normalizedName = normalize(product.name),
        imageUrl = product.imageUrl,
        calories = product.calories,
        fats = product.fats,
        proteins = product.proteins,
        carbs = product.carbs,
        sugars = product.sugars,
        brands = product.brands,
        quantity = product.quantity,
        servingSize = product.servingSize,
        ingredientsJson = product.ingredients,
        allergensJson = gson.toJson(product.allergens),
        categoriesJson = gson.toJson(product.categories),
        nutritionGrade = product.nutritionGrade,
        novaGroup = product.novaGroup,
        nutrientsJson = gson.toJson(product.nutrients),
        updatedAt = updatedAt
    )

    fun toDomain(entity: ProductCacheEntity) = Product(
        code = entity.barcode ?: if (entity.source == FoodSource.LOCAL_FRESH) "fresh:${entity.sourceId}" else entity.sourceId,
        name = entity.name,
        imageUrl = entity.imageUrl,
        calories = entity.calories,
        fats = entity.fats,
        proteins = entity.proteins,
        carbs = entity.carbs,
        sugars = entity.sugars,
        brands = entity.brands,
        quantity = entity.quantity,
        servingSize = entity.servingSize,
        ingredients = entity.ingredientsJson,
        allergens = gson.fromJson(entity.allergensJson, stringListType),
        categories = gson.fromJson(entity.categoriesJson, stringListType),
        nutritionGrade = entity.nutritionGrade,
        novaGroup = entity.novaGroup,
        nutrients = gson.fromJson(entity.nutrientsJson, nutrientListType)
    )

    fun key(source: String, sourceId: String) = "$source:$sourceId"

    fun normalize(value: String): String = Normalizer.normalize(value.lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")
        .trim()
}
