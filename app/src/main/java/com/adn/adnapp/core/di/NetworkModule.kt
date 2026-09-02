package com.adn.adnapp.core.di

import com.adn.adnapp.core.constants.ApiConstants
import com.adn.adnapp.data.remote.api.OpenFoodFactsApi
import com.adn.adnapp.data.remote.api.FoodSearchApi
import okhttp3.OkHttpClient
import org.koin.dsl.module
import org.koin.core.qualifier.named
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    single {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", "ADNApp/1.0 (https://github.com/Vicserra22/ADNApp)")
                        .header("Accept-Language", "es")
                        .build()
                )
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    single(named("openFoodFacts")) {
        Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    single(named("foodSearch")) {
        Retrofit.Builder()
            .baseUrl(ApiConstants.SEARCH_BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    single { get<Retrofit>(named("openFoodFacts")).create(OpenFoodFactsApi::class.java) }
    single { get<Retrofit>(named("foodSearch")).create(FoodSearchApi::class.java) }
}
