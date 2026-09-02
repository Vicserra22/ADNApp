package com.adn.adnapp.data.remote.api

import com.adn.adnapp.data.model.dto.SearchFoodRequest
import com.adn.adnapp.data.model.dto.SearchFoodResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface FoodSearchApi {
    @POST("search")
    suspend fun searchFoods(@Body request: SearchFoodRequest): SearchFoodResponse
}
