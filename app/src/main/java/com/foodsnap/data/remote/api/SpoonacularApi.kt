package com.foodsnap.data.remote.api

import com.foodsnap.data.remote.dto.spoonacular.RandomRecipesResponse
import com.foodsnap.data.remote.dto.spoonacular.RecipeDetailsDto
import com.foodsnap.data.remote.dto.spoonacular.RecipeSearchResponse
import com.foodsnap.data.remote.dto.spoonacular.RecipesByIngredientsResponseItem
import com.foodsnap.data.remote.dto.spoonacular.SimilarRecipeDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface for Spoonacular Recipe API.
 *
 * Base URL: https://api.spoonacular.com/
 * API Key is added via interceptor.
 *
 * Free tier: 150 requests/day
 * Documentation: https://spoonacular.com/food-api/docs
 */
interface SpoonacularApi {

    companion object {
        const val BASE_URL = "https://api.spoonacular.com/"
    }

    /**
     * Search recipes with various filters.
     *
     * @param query Search text
     * @param cuisine Filter by cuisine type
     * @param diet Filter by diet type
     * @param type Filter by meal type
     * @param maxReadyTime Maximum cooking time in minutes
     * @param number Number of results (max 100)
     * @param offset Pagination offset
     * @param addRecipeInformation Include full recipe info
     * @param fillIngredients Include ingredients list
     */
    @GET("recipes/complexSearch")
    suspend fun searchRecipes(
        @Query("query") query: String,
        @Query("cuisine") cuisine: String? = null,
        @Query("diet") diet: String? = null,
        @Query("type") type: String? = null,
        @Query("maxReadyTime") maxReadyTime: Int? = null,
        @Query("number") number: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("addRecipeInformation") addRecipeInformation: Boolean = true,
        @Query("fillIngredients") fillIngredients: Boolean = true
    ): RecipeSearchResponse

    /**
     * Find recipes by available ingredients.
     *
     * @param ingredients Comma-separated list of ingredients
     * @param number Number of results (max 100)
     * @param ranking 1 = maximize used ingredients, 2 = minimize missing ingredients
     * @param ignorePantry Whether to ignore common pantry items
     */
    @GET("recipes/findByIngredients")
    suspend fun findByIngredients(
        @Query("ingredients") ingredients: String,
        @Query("number") number: Int = 20,
        @Query("ranking") ranking: Int = 1,
        @Query("ignorePantry") ignorePantry: Boolean = false
    ): List<RecipesByIngredientsResponseItem>

    /**
     * Get detailed recipe information.
     *
     * @param id Recipe ID
     * @param includeNutrition Include nutrition data
     */
    @GET("recipes/{id}/information")
    suspend fun getRecipeInformation(
        @Path("id") id: Int,
        @Query("includeNutrition") includeNutrition: Boolean = true
    ): RecipeDetailsDto

    /**
     * Get similar recipes.
     *
     * @param id Recipe ID to find similar recipes for
     * @param number Number of results
     */
    @GET("recipes/{id}/similar")
    suspend fun getSimilarRecipes(
        @Path("id") id: Int,
        @Query("number") number: Int = 10
    ): List<SimilarRecipeDto>

    /**
     * Get random recipes.
     *
     * @param number Number of random recipes
     * @param tags Comma-separated tags to filter by
     */
    @GET("recipes/random")
    suspend fun getRandomRecipes(
        @Query("number") number: Int = 10,
        @Query("tags") tags: String? = null
    ): RandomRecipesResponse

    /**
     * Autocomplete ingredient search.
     *
     * @param query Partial ingredient name
     * @param number Number of suggestions
     */
    @GET("food/ingredients/autocomplete")
    suspend fun autocompleteIngredient(
        @Query("query") query: String,
        @Query("number") number: Int = 10
    ): List<IngredientAutocompleteDto>
}

/**
 * DTO for ingredient autocomplete response.
 */
data class IngredientAutocompleteDto(
    val name: String,
    val image: String?,
    val id: Int?,
    val aisle: String?
)
