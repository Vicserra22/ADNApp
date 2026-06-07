package com.adn.adnapp.data.remote.api

import com.adn.adnapp.data.model.dto.FoodResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenFoodFactsApi {
    @GET("cgi/search.pl")
    suspend fun searchFoods(
        @Query("search_terms") query: String,
        @Query("search_simple") searchSimple: Int = 1,
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1,
        @Query("fields") fields: String = "code,product_name,image_front_url,nutriments",
        @Query("page_size") pageSize: Int = 50
    ): FoodResponseDto
}
