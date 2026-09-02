package com.adn.adnapp.data.model.dto

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class SearchFoodRequest(
    val q: String,
    val fields: List<String> = SEARCH_FIELDS,
    @SerializedName("page_size") val pageSize: Int = 20,
    val page: Int = 1,
    @SerializedName("boost_phrase") val boostPhrase: Boolean = true,
    val langs: List<String> = listOf("es", "en")
) {
    companion object {
        val SEARCH_FIELDS = listOf(
            "code", "product_name", "product_name_es", "generic_name_es",
            "image_front_small_url", "image_front_url", "brands", "quantity",
            "serving_size", "ingredients_text", "ingredients_text_es",
            "allergens_tags", "categories_tags", "nutrition_grades",
            "nutriscore_grade", "nova_group", "nutriments"
        )
    }
}

data class SearchFoodResponse(
    val hits: List<SearchFoodHit> = emptyList(),
    val page: Int = 1,
    @SerializedName("page_count") val pageCount: Int = 0
)

data class SearchFoodHit(
    val code: String? = null,
    @SerializedName("product_name") val name: String? = null,
    @SerializedName("product_name_es") val spanishName: String? = null,
    @SerializedName("generic_name_es") val spanishGenericName: String? = null,
    @SerializedName("image_front_small_url") val smallImageUrl: String? = null,
    @SerializedName("image_front_url") val imageUrl: String? = null,
    val brands: List<String>? = null,
    val quantity: String? = null,
    @SerializedName("serving_size") val servingSize: String? = null,
    @SerializedName("ingredients_text") val ingredients: String? = null,
    @SerializedName("ingredients_text_es") val spanishIngredients: String? = null,
    @SerializedName("allergens_tags") val allergens: List<String>? = null,
    @SerializedName("categories_tags") val categories: List<String>? = null,
    @SerializedName("nutrition_grades") val nutritionGrade: String? = null,
    @SerializedName("nutriscore_grade") val nutriScoreGrade: String? = null,
    @SerializedName("nova_group") val novaGroup: Int? = null,
    val nutriments: JsonObject? = null
)
