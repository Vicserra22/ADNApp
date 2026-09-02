package com.adn.adnapp.data.repository

import com.adn.adnapp.data.mapper.ProductMapper
import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.data.model.entity.FoodEntry
import com.adn.adnapp.data.model.entity.Product
import com.adn.adnapp.data.remote.api.OpenFoodFactsApi
import com.adn.adnapp.data.remote.firebase.FirestoreDataSource
import com.adn.adnapp.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException

class FoodSearchException(val reason: Reason) : Exception() {
    enum class Reason { RATE_LIMIT, SERVICE_UNAVAILABLE, NETWORK }
}

class FoodRepositoryImpl(
    private val api: OpenFoodFactsApi,
    private val firestoreDataSource: FirestoreDataSource
) : FoodRepository {
    private val searchCache = object : LinkedHashMap<String, List<Product>>(20, .75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<Product>>?) = size > 20
    }
    private val searchMutex = Mutex()
    private var lastApiSearchAt = 0L

    override suspend fun searchFoods(query: String): Result<List<Product>> {
        val normalizedQuery = query.trim().lowercase()
        synchronized(searchCache) { searchCache[normalizedQuery] }?.let { return Result.success(it) }
        return searchMutex.withLock {
            synchronized(searchCache) { searchCache[normalizedQuery] }?.let { return@withLock Result.success(it) }
            val now = System.nanoTime() / 1_000_000
            val elapsed = now - lastApiSearchAt
            if (lastApiSearchAt > 0 && elapsed < SEARCH_INTERVAL_MS) delay(SEARCH_INTERVAL_MS - elapsed)
            lastApiSearchAt = System.nanoTime() / 1_000_000
            try {
                val response = api.searchFoods(query = query.trim())
                val products = response.products?.map { ProductMapper.mapToDomain(it) }
                    ?.filter { it.name.isNotBlank() && it.code.isNotBlank() &&
                        (it.calories > 0 || it.proteins > 0 || it.carbs > 0 || it.fats > 0) }
                    ?.distinctBy { it.code }
                    ?: emptyList()
                synchronized(searchCache) { searchCache[normalizedQuery] = products }
                Result.success(products)
            } catch (error: HttpException) {
                Result.failure(
                    FoodSearchException(
                        when (error.code()) {
                            429 -> FoodSearchException.Reason.RATE_LIMIT
                            503 -> FoodSearchException.Reason.SERVICE_UNAVAILABLE
                            else -> FoodSearchException.Reason.NETWORK
                        }
                    )
                )
            } catch (_: Exception) {
                Result.failure(FoodSearchException(FoodSearchException.Reason.NETWORK))
            }
        }
    }

    private companion object { const val SEARCH_INTERVAL_MS = 6_100L }

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
