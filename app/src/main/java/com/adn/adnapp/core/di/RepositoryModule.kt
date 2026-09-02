package com.adn.adnapp.core.di

import com.adn.adnapp.data.repository.AuthRepositoryImpl
import com.adn.adnapp.data.repository.DietRepositoryImpl
import com.adn.adnapp.data.repository.FoodRepositoryImpl
import com.adn.adnapp.data.repository.NutritionRepositoryImpl
import com.adn.adnapp.data.repository.UserRepositoryImpl
import com.adn.adnapp.data.repository.WeightRepositoryImpl
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.DietRepository
import com.adn.adnapp.domain.repository.FoodRepository
import com.adn.adnapp.domain.repository.NutritionRepository
import com.adn.adnapp.domain.repository.UserRepository
import com.adn.adnapp.domain.repository.WeightRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<FoodRepository> { FoodRepositoryImpl(get(), get()) }
    single<DietRepository> { DietRepositoryImpl(get()) }
    single<NutritionRepository> { NutritionRepositoryImpl(get()) }
    single<WeightRepository> { WeightRepositoryImpl(get()) }
}
