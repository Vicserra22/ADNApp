package com.adn.adnapp.data.local.food

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodCacheDao {
    @Upsert
    suspend fun upsertProducts(products: List<ProductCacheEntity>)

    @Query("SELECT * FROM food_products WHERE `key` = :key LIMIT 1")
    suspend fun product(key: String): ProductCacheEntity?

    @Query("SELECT * FROM food_products WHERE barcode = :barcode LIMIT 1")
    suspend fun productByBarcode(barcode: String): ProductCacheEntity?

    @Query(
        """SELECT p.* FROM food_products p
           INNER JOIN food_search_results s ON s.productKey = p.`key`
           WHERE s.normalizedQuery = :query ORDER BY s.rank LIMIT :limit"""
    )
    suspend fun cachedSearch(query: String, limit: Int): List<ProductCacheEntity>

    @Query("SELECT MAX(fetchedAt) FROM food_search_results WHERE normalizedQuery = :query")
    suspend fun searchFetchedAt(query: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchResults(results: List<SearchResultEntity>)

    @Query("DELETE FROM food_search_results WHERE normalizedQuery = :query")
    suspend fun deleteSearchResults(query: String)

    @Transaction
    suspend fun replaceSearchResults(query: String, products: List<ProductCacheEntity>, fetchedAt: Long) {
        upsertProducts(products)
        deleteSearchResults(query)
        insertSearchResults(products.mapIndexed { index, product ->
            SearchResultEntity(query, product.key, index, fetchedAt)
        })
    }

    @Query(
        """SELECT * FROM food_products
           WHERE source = :source AND (normalizedName LIKE '%' || :query || '%'
             OR categoriesJson LIKE '%' || :query || '%')
           ORDER BY name COLLATE NOCASE LIMIT :limit"""
    )
    suspend fun searchLocal(query: String, source: String = FoodSource.LOCAL_FRESH, limit: Int = 30): List<ProductCacheEntity>

    @Query("SELECT * FROM food_products WHERE source = :source ORDER BY name COLLATE NOCASE")
    fun observeFreshCatalog(source: String = FoodSource.LOCAL_FRESH): Flow<List<ProductCacheEntity>>

    @Query("SELECT COUNT(*) FROM food_products WHERE source = :source")
    suspend fun countBySource(source: String = FoodSource.LOCAL_FRESH): Int

    @Query("SELECT * FROM recent_foods WHERE userId = :userId AND productKey = :productKey LIMIT 1")
    suspend fun recent(userId: String, productKey: String): RecentFoodEntity?

    @Upsert
    suspend fun upsertRecent(recent: RecentFoodEntity)

    @Transaction
    suspend fun recordRecent(userId: String, productKey: String, usedAt: Long) {
        val old = recent(userId, productKey)
        upsertRecent(RecentFoodEntity(userId, productKey, usedAt, (old?.useCount ?: 0) + 1))
    }

    @Query(
        """SELECT p.*, r.lastUsedAt, r.useCount FROM recent_foods r
           INNER JOIN food_products p ON p.`key` = r.productKey
           WHERE r.userId = :userId ORDER BY r.lastUsedAt DESC LIMIT :limit"""
    )
    fun observeRecents(userId: String, limit: Int = 20): Flow<List<RecentProductRow>>

    @Upsert
    suspend fun upsertFavorite(favorite: FavoriteFoodEntity)

    @Query("DELETE FROM favorite_foods WHERE userId = :userId AND productKey = :productKey")
    suspend fun deleteFavorite(userId: String, productKey: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_foods WHERE userId = :userId AND productKey = :productKey)")
    suspend fun isFavorite(userId: String, productKey: String): Boolean

    @Transaction
    suspend fun toggleFavorite(userId: String, productKey: String, savedAt: Long): Boolean {
        val nowFavorite = !isFavorite(userId, productKey)
        if (nowFavorite) upsertFavorite(FavoriteFoodEntity(userId, productKey, savedAt))
        else deleteFavorite(userId, productKey)
        return nowFavorite
    }

    @Query(
        """SELECT p.*, f.savedAt FROM favorite_foods f
           INNER JOIN food_products p ON p.`key` = f.productKey
           WHERE f.userId = :userId ORDER BY f.savedAt DESC"""
    )
    fun observeFavorites(userId: String): Flow<List<FavoriteProductRow>>

    @Query("DELETE FROM food_search_results WHERE fetchedAt < :before")
    suspend fun pruneSearches(before: Long)
}
