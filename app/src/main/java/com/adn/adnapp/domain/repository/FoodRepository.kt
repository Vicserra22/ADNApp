package com.adn.adnapp.domain.repository

import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.data.model.entity.FoodEntry
import com.adn.adnapp.data.model.entity.Product
import com.adn.adnapp.domain.model.DailyActivityLevel
import kotlinx.coroutines.flow.Flow

interface FoodRepository {
    suspend fun searchFoods(query: String): Result<List<Product>>
    suspend fun getFoodByBarcode(barcode: String): Result<Product>
    suspend fun searchFreshFoods(query: String): List<Product>
    fun observeFreshFoods(): Flow<List<Product>>
    fun observeRecentFoods(uid: String): Flow<List<Product>>
    fun observeFavoriteFoods(uid: String): Flow<List<Product>>
    suspend fun isFavorite(uid: String, product: Product): Boolean
    suspend fun toggleFavorite(uid: String, product: Product): Result<Boolean>
    suspend fun saveCustomDish(uid: String, product: Product): Result<Unit>
    suspend fun recordProductUsed(uid: String, product: Product)
    suspend fun saveFoodEntry(uid: String, entry: FoodEntry, dateKey: String): Result<Unit>
    suspend fun saveWaterEntry(uid: String, dateKey: String, amountMl: Double): Result<String>
    suspend fun deleteWaterEntry(uid: String, dateKey: String, entryId: String): Result<Unit>
    suspend fun updateFoodEntry(uid: String, entry: FoodEntry, dateKey: String): Result<Unit>
    suspend fun deleteFoodEntry(uid: String, entryId: String, dateKey: String): Result<Unit>
    suspend fun setDailyActivityLevel(uid: String, dateKey: String, level: DailyActivityLevel): Result<Unit>
    fun observeFoodEntries(uid: String, dateKey: String): Flow<List<FoodEntry>>
    fun observeDailyConsumption(uid: String, dateKey: String): Flow<DailyConsumption>
    fun observeConsumptionHistory(uid: String): Flow<Map<String, DailyConsumption>>
}
