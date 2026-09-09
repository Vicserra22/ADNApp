package com.adn.adnapp.data.repository

import com.adn.adnapp.data.mapper.ProductMapper
import com.adn.adnapp.data.local.food.FoodCacheDao
import com.adn.adnapp.data.local.food.FoodSource
import com.adn.adnapp.data.local.food.ProductCacheMapper
import com.adn.adnapp.data.model.dto.SearchFoodRequest
import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.data.model.entity.FoodEntry
import com.adn.adnapp.data.model.entity.Product
import com.adn.adnapp.data.remote.api.OpenFoodFactsApi
import com.adn.adnapp.data.remote.api.FoodSearchApi
import com.adn.adnapp.data.remote.firebase.FirestoreDataSource
import com.adn.adnapp.domain.repository.FoodRepository
import com.adn.adnapp.domain.model.DailyActivityLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException

class FoodSearchException(val reason: Reason) : Exception() {
    enum class Reason { RATE_LIMIT, SERVICE_UNAVAILABLE, NETWORK }
}

class FoodRepositoryImpl(
    private val api: OpenFoodFactsApi,
    private val searchApi: FoodSearchApi,
    private val cacheDao: FoodCacheDao,
    private val firestoreDataSource: FirestoreDataSource
) : FoodRepository {
    private val searchMutex = Mutex()
    private var lastApiSearchAt = 0L

    override suspend fun searchFoods(query: String): Result<List<Product>> {
        val normalizedQuery = ProductCacheMapper.normalize(query)
        val cached = cacheDao.cachedSearch(normalizedQuery, SEARCH_LIMIT).map(ProductCacheMapper::toDomain)
        val cachedAt = cacheDao.searchFetchedAt(normalizedQuery) ?: 0L
        if (cached.isNotEmpty() && System.currentTimeMillis() - cachedAt < CACHE_MAX_AGE_MS) {
            return Result.success(cached)
        }
        return searchMutex.withLock {
            val now = System.nanoTime() / 1_000_000
            val elapsed = now - lastApiSearchAt
            if (lastApiSearchAt > 0 && elapsed < SEARCH_INTERVAL_MS) delay(SEARCH_INTERVAL_MS - elapsed)
            lastApiSearchAt = System.nanoTime() / 1_000_000
            try {
                val response = searchApi.searchFoods(SearchFoodRequest(q = query.trim()))
                val products = response.hits.map(ProductMapper::mapToDomain)
                    .filter { it.name.isNotBlank() && it.code.isNotBlank() &&
                        (it.calories > 0 || it.proteins > 0 || it.carbs > 0 || it.fats > 0) }
                    .distinctBy { it.code }
                val fetchedAt = System.currentTimeMillis()
                cacheDao.replaceSearchResults(normalizedQuery, products.map {
                    ProductCacheMapper.toEntity(it, FoodSource.OPEN_FOOD_FACTS, updatedAt = fetchedAt)
                }, fetchedAt)
                cacheDao.pruneSearches(fetchedAt - CACHE_PRUNE_AGE_MS)
                Result.success(products)
            } catch (error: HttpException) {
                if (cached.isNotEmpty()) Result.success(cached) else Result.failure(
                    FoodSearchException(
                        when (error.code()) {
                            429 -> FoodSearchException.Reason.RATE_LIMIT
                            503 -> FoodSearchException.Reason.SERVICE_UNAVAILABLE
                            else -> FoodSearchException.Reason.NETWORK
                        }
                    )
                )
            } catch (_: Exception) {
                if (cached.isNotEmpty()) Result.success(cached)
                else Result.failure(FoodSearchException(FoodSearchException.Reason.NETWORK))
            }
        }
    }

    override suspend fun getFoodByBarcode(barcode: String): Result<Product> = runCatching {
        val normalized = barcode.filter(Char::isDigit).also {
            require(it.length in 8..14) { "Código de barras no válido" }
        }
        cacheDao.productByBarcode(normalized)?.let(ProductCacheMapper::toDomain) ?: run {
            val product = api.getFoodByBarcode(normalized).product?.let(ProductMapper::mapToDomain)
                ?.takeIf { it.name.isNotBlank() }
                ?: error("Producto no encontrado")
            cacheDao.upsertProducts(listOf(ProductCacheMapper.toEntity(product, FoodSource.OPEN_FOOD_FACTS)))
            product
        }
    }

    override suspend fun searchFreshFoods(query: String): List<Product> =
        cacheDao.searchLocal(ProductCacheMapper.normalize(query)).map(ProductCacheMapper::toDomain)

    override fun observeFreshFoods(): Flow<List<Product>> =
        cacheDao.observeFreshCatalog().map { values -> values.map(ProductCacheMapper::toDomain) }

    override fun observeRecentFoods(uid: String): Flow<List<Product>> =
        cacheDao.observeRecents(uid).map { values -> values.map { ProductCacheMapper.toDomain(it.product) } }

    override fun observeFavoriteFoods(uid: String): Flow<List<Product>> =
        cacheDao.observeFavorites(uid).map { values -> values.map { ProductCacheMapper.toDomain(it.product) } }

    override suspend fun isFavorite(uid: String, product: Product): Boolean =
        cacheDao.isFavorite(uid, product.cacheKey())

    override suspend fun toggleFavorite(uid: String, product: Product): Result<Boolean> = runCatching {
        val entity = product.cacheEntity()
        cacheDao.upsertProducts(listOf(entity))
        cacheDao.toggleFavorite(uid, entity.key, System.currentTimeMillis())
    }

    override suspend fun recordProductUsed(uid: String, product: Product) {
        val entity = product.cacheEntity()
        cacheDao.upsertProducts(listOf(entity))
        cacheDao.recordRecent(uid, entity.key, System.currentTimeMillis())
    }

    override suspend fun saveCustomDish(uid: String, product: Product): Result<Unit> = runCatching {
        require(product.code.startsWith("dish:$uid:"))
        cacheDao.saveCustomDish(uid, product.cacheEntity())
    }

    private fun Product.cacheEntity() = if (code.startsWith("fresh:")) {
        ProductCacheMapper.toEntity(this, FoodSource.LOCAL_FRESH, code.removePrefix("fresh:"), null)
    } else ProductCacheMapper.toEntity(this, if (code.startsWith("dish:")) "custom_dish" else FoodSource.OPEN_FOOD_FACTS)

    private fun Product.cacheKey() = cacheEntity().key

    private companion object {
        const val SEARCH_INTERVAL_MS = 700L
        const val SEARCH_LIMIT = 20
        const val CACHE_MAX_AGE_MS = 7 * 24 * 60 * 60 * 1_000L
        const val CACHE_PRUNE_AGE_MS = 30 * 24 * 60 * 60 * 1_000L
    }

    override suspend fun saveFoodEntry(uid: String, entry: FoodEntry, dateKey: String): Result<Unit> {
        return try {
            firestoreDataSource.saveFoodEntryAndAggregate(uid, entry, dateKey)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveWaterEntry(uid: String, dateKey: String, amountMl: Double): Result<String> =
        runCatching { firestoreDataSource.saveWaterEntryAndAggregate(uid, dateKey, amountMl) }

    override suspend fun deleteWaterEntry(uid: String, dateKey: String, entryId: String): Result<Unit> =
        runCatching { firestoreDataSource.deleteFoodEntryAndAggregate(uid, entryId, dateKey) }

    override suspend fun updateFoodEntry(uid: String, entry: FoodEntry, dateKey: String): Result<Unit> =
        runCatching { firestoreDataSource.updateFoodEntryAndAggregate(uid, entry, dateKey) }

    override suspend fun deleteFoodEntry(uid: String, entryId: String, dateKey: String): Result<Unit> =
        runCatching { firestoreDataSource.deleteFoodEntryAndAggregate(uid, entryId, dateKey) }

    override suspend fun setDailyActivityLevel(
        uid: String,
        dateKey: String,
        level: DailyActivityLevel
    ): Result<Unit> = runCatching { firestoreDataSource.setDailyActivityLevel(uid, dateKey, level) }

    override fun observeFoodEntries(uid: String, dateKey: String): Flow<List<FoodEntry>> =
        firestoreDataSource.observeFoodEntries(uid, dateKey)

    override fun observeDailyConsumption(uid: String, dateKey: String): Flow<DailyConsumption> {
        return firestoreDataSource.observeDailyConsumption(uid, dateKey)
    }

    override fun observeConsumptionHistory(uid: String): Flow<Map<String, DailyConsumption>> {
        return firestoreDataSource.observeConsumptionHistory(uid)
    }
}
