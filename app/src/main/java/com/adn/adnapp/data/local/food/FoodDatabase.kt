package com.adn.adnapp.data.local.food

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProductCacheEntity::class,
        SearchResultEntity::class,
        RecentFoodEntity::class,
        FavoriteFoodEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class FoodDatabase : RoomDatabase() {
    abstract fun foodCacheDao(): FoodCacheDao
}
