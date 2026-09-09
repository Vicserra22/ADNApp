package com.adn.adnapp.core.di

import androidx.room.Room
import com.adn.adnapp.data.local.food.FoodCatalogInitializer
import com.adn.adnapp.data.local.food.FoodDatabase
import com.adn.adnapp.data.local.WellnessStore
import com.adn.adnapp.data.local.AgendaReminderScheduler
import com.adn.adnapp.data.local.AgendaStore
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
    single { AgendaStore(get()) }
    single { AgendaReminderScheduler(get()) }
}
