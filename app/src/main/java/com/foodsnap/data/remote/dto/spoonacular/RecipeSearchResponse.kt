package com.foodsnap.data.remote.dto.spoonacular

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response from /recipes/complexSearch endpoint.
 */
@JsonClass(generateAdapter = true)
data class RecipeSearchResponse(
    @Json(name = "results") val results: List<RecipeSearchResultDto>,
    @Json(name = "offset") val offset: Int,
    @Json(name = "number") val number: Int,
    @Json(name = "totalResults") val totalResults: Int
)

/**
 * Individual recipe in search results.
 */
@JsonClass(generateAdapter = true)
data class RecipeSearchResultDto(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "image") val image: String?,
    @Json(name = "imageType") val imageType: String?,
    @Json(name = "servings") val servings: Int?,
    @Json(name = "readyInMinutes") val readyInMinutes: Int?,
    @Json(name = "sourceUrl") val sourceUrl: String?,
    @Json(name = "summary") val summary: String?,
    @Json(name = "cuisines") val cuisines: List<String>?,
    @Json(name = "dishTypes") val dishTypes: List<String>?,
    @Json(name = "diets") val diets: List<String>?,
    @Json(name = "nutrition") val nutrition: NutritionDto?
)

/**
 * Nutrition information.
 */
@JsonClass(generateAdapter = true)
data class NutritionDto(
    @Json(name = "nutrients") val nutrients: List<NutrientDto>?
)

/**
 * Individual nutrient.
 */
@JsonClass(generateAdapter = true)
data class NutrientDto(
    @Json(name = "name") val name: String,
    @Json(name = "amount") val amount: Double,
    @Json(name = "unit") val unit: String,
    @Json(name = "percentOfDailyNeeds") val percentOfDailyNeeds: Double?
)
