package com.adn.adnapp.core.di

import androidx.room.Room
import com.adn.adnapp.data.local.food.FoodCatalogInitializer
import com.adn.adnapp.data.local.food.FoodDatabase
import com.adn.adnapp.data.local.WellnessStore
import org.koin.dsl.module

val localFoodModule = module {
    single {
        Room.databaseBuilder(
            get(),
            FoodDatabase::class.java,
            "adn-food-cache.db"
        ).build()
    }
    single { get<FoodDatabase>().foodCacheDao() }
    single(createdAtStart = true) { FoodCatalogInitializer(get()).also { it.start() } }
    single { WellnessStore(get()) }
}
