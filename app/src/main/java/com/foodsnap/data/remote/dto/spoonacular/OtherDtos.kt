package com.foodsnap.data.remote.dto.spoonacular

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response from /recipes/random endpoint.
 */
@JsonClass(generateAdapter = true)
data class RandomRecipesResponse(
    @Json(name = "recipes") val recipes: List<RecipeDetailsDto>
)

/**
 * Response item from /recipes/findByIngredients endpoint.
 */
@JsonClass(generateAdapter = true)
data class RecipesByIngredientsResponseItem(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "image") val image: String?,
    @Json(name = "imageType") val imageType: String?,
    @Json(name = "usedIngredientCount") val usedIngredientCount: Int,
    @Json(name = "missedIngredientCount") val missedIngredientCount: Int,
    @Json(name = "usedIngredients") val usedIngredients: List<IngredientMatchDto>?,
    @Json(name = "missedIngredients") val missedIngredients: List<IngredientMatchDto>?,
    @Json(name = "unusedIngredients") val unusedIngredients: List<IngredientMatchDto>?,
    @Json(name = "likes") val likes: Int?
)

/**
 * Ingredient match information from find-by-ingredients.
 */
@JsonClass(generateAdapter = true)
data class IngredientMatchDto(
    @Json(name = "id") val id: Int,
    @Json(name = "amount") val amount: Double?,
    @Json(name = "unit") val unit: String?,
    @Json(name = "unitLong") val unitLong: String?,
    @Json(name = "unitShort") val unitShort: String?,
    @Json(name = "aisle") val aisle: String?,
    @Json(name = "name") val name: String,
    @Json(name = "original") val original: String?,
    @Json(name = "originalName") val originalName: String?,
    @Json(name = "meta") val meta: List<String>?,
    @Json(name = "image") val image: String?
)

/**
 * Similar recipe information from /recipes/{id}/similar endpoint.
 */
@JsonClass(generateAdapter = true)
data class SimilarRecipeDto(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "imageType") val imageType: String?,
    @Json(name = "readyInMinutes") val readyInMinutes: Int?,
    @Json(name = "servings") val servings: Int?,
    @Json(name = "sourceUrl") val sourceUrl: String?
)
