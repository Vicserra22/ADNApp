package com.adn.adnapp.domain.repository

import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.data.model.entity.FoodEntry
import com.adn.adnapp.data.model.entity.Product
import kotlinx.coroutines.flow.Flow

interface FoodRepository {
    suspend fun searchFoods(query: String): Result<List<Product>>
    suspend fun saveFoodEntry(uid: String, entry: FoodEntry, dateKey: String): Result<Unit>
    suspend fun setDailyConsumption(uid: String, consumption: DailyConsumption): Result<Unit>
    fun observeDailyConsumption(uid: String, dateKey: String): Flow<DailyConsumption>
    fun observeConsumptionHistory(uid: String): Flow<Map<String, DailyConsumption>>
}
