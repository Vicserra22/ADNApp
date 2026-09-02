package com.adn.adnapp.data.local.food

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "food_products",
    primaryKeys = ["key"],
    indices = [Index("barcode"), Index("source"), Index("normalizedName")]
)
data class ProductCacheEntity(
    val key: String,
    val source: String,
    val sourceId: String,
    val barcode: String?,
    val name: String,
    val normalizedName: String,
    val imageUrl: String?,
    val calories: Double,
    val fats: Double,
    val proteins: Double,
    val carbs: Double,
    val sugars: Double,
    val brands: String,
    val quantity: String,
    val servingSize: String,
    val ingredientsJson: String,
    val allergensJson: String,
    val categoriesJson: String,
    val nutritionGrade: String,
    val novaGroup: Int?,
    val nutrientsJson: String,
    val updatedAt: Long
)

@Entity(
    tableName = "food_search_results",
    primaryKeys = ["normalizedQuery", "productKey"],
    indices = [Index("productKey"), Index("fetchedAt")]
)
data class SearchResultEntity(
    val normalizedQuery: String,
    val productKey: String,
    val rank: Int,
    val fetchedAt: Long
)

@Entity(
    tableName = "recent_foods",
    primaryKeys = ["userId", "productKey"],
    indices = [Index("productKey"), Index("lastUsedAt")]
)
data class RecentFoodEntity(
    val userId: String,
    val productKey: String,
    val lastUsedAt: Long,
    val useCount: Int
)

@Entity(
    tableName = "favorite_foods",
    primaryKeys = ["userId", "productKey"],
    indices = [Index("productKey"), Index("savedAt")]
)
data class FavoriteFoodEntity(
    val userId: String,
    val productKey: String,
    val savedAt: Long
)

data class RecentProductRow(
    @androidx.room.Embedded val product: ProductCacheEntity,
    val lastUsedAt: Long,
    val useCount: Int
)

data class FavoriteProductRow(
    @androidx.room.Embedded val product: ProductCacheEntity,
    val savedAt: Long
)

object FoodSource {
    const val OPEN_FOOD_FACTS = "open_food_facts"
    const val USDA = "usda"
    const val LOCAL_FRESH = "local_fresh"
}
