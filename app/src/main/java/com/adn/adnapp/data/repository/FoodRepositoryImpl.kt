package com.adn.adnapp.data.repository

import com.adn.adnapp.data.mapper.ProductMapper
import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.data.model.entity.FoodEntry
import com.adn.adnapp.data.model.entity.Product
import com.adn.adnapp.data.remote.api.OpenFoodFactsApi
import com.adn.adnapp.data.remote.firebase.FirestoreDataSource
import com.adn.adnapp.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow

class FoodRepositoryImpl(
    private val api: OpenFoodFactsApi,
    private val firestoreDataSource: FirestoreDataSource
) : FoodRepository {

    override suspend fun searchFoods(query: String): Result<List<Product>> {
        return try {
            val response = api.searchFoods(query = query)
            val products = response.products?.map { ProductMapper.mapToDomain(it) }
                ?.filter { it.name.isNotBlank() && it.name != "Unknown" }
                ?.distinctBy { it.name.lowercase() }
                ?: emptyList()
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveFoodEntry(uid: String, entry: FoodEntry, dateKey: String): Result<Unit> {
        return try {
            firestoreDataSource.saveFoodEntryAndAggregate(uid, entry, dateKey)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateFoodEntry(uid: String, entry: FoodEntry, dateKey: String): Result<Unit> =
        runCatching { firestoreDataSource.updateFoodEntryAndAggregate(uid, entry, dateKey) }

    override suspend fun deleteFoodEntry(uid: String, entryId: String, dateKey: String): Result<Unit> =
        runCatching { firestoreDataSource.deleteFoodEntryAndAggregate(uid, entryId, dateKey) }

    override fun observeFoodEntries(uid: String, dateKey: String): Flow<List<FoodEntry>> =
        firestoreDataSource.observeFoodEntries(uid, dateKey)

    override fun observeDailyConsumption(uid: String, dateKey: String): Flow<DailyConsumption> {
        return firestoreDataSource.observeDailyConsumption(uid, dateKey)
    }

    override fun observeConsumptionHistory(uid: String): Flow<Map<String, DailyConsumption>> {
        return firestoreDataSource.observeConsumptionHistory(uid)
    }
}
