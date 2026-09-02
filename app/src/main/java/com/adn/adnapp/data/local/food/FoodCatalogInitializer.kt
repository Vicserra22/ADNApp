package com.adn.adnapp.data.local.food

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FoodCatalogInitializer(private val dao: FoodCacheDao) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start() {
        scope.launch { ensureSeeded() }
    }

    suspend fun ensureSeeded() {
        if (dao.countBySource() < FreshFoodCatalog.products.size) {
            dao.upsertProducts(FreshFoodCatalog.entities())
        }
    }
}
