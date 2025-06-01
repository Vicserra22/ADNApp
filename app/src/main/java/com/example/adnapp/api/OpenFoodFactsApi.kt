package com.example.adnapp.api

import com.example.adnapp.models.FoodResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

interface OpenFoodFactsApi {
    @GET("cgi/search.pl")
    suspend fun searchFoods(
        @Query("search_terms") searchTerms: String,
        @Query("action") action: String = "process",
        @Query("search_simple") searchSimple: Int = 1,
        @Query("json") json: Int = 1,
        @Query("sort_by") sortBy: String = "unique_scans_n",
        @Query("fields") fields: String = "code,product_name,brands,quantity,nutrition_grades,nutriments,image_front_url",
        @Query("page_size") pageSize: Int = 100
    ):Response<FoodResponse>
}

