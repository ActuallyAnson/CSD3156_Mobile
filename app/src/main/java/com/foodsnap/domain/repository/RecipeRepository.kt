package com.foodsnap.domain.repository

import com.foodsnap.domain.model.Recipe
import com.foodsnap.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for recipe operations.
 *
 * Defines the contract for accessing recipe data from any source
 * (network, database, etc.). Implementations handle the actual
 * data fetching and caching logic.
 */
interface RecipeRepository {

    /**
     * Searches for recipes matching the query.
     *
     * @param query Search text
     * @param cuisine Optional cuisine filter
     * @param diet Optional diet filter
     * @param type Optional meal type filter
     * @return Flow emitting Resource with search results
     */
    fun searchRecipes(
        query: String,
        cuisine: String? = null,
        diet: String? = null,
        type: String? = null
    ): Flow<Resource<List<Recipe>>>

    /**
     * Gets a single recipe by ID.
     *
     * @param recipeId The recipe ID
     * @return Flow emitting Resource with the recipe
     */
    fun getRecipeById(recipeId: Long): Flow<Resource<Recipe>>

    /**
     * Gets random recipes for featured/discovery sections.
     *
     * @param count Number of recipes to fetch
     * @param tags Optional tags to filter by
     * @return Flow emitting Resource with random recipes
     */
    fun getRandomRecipes(count: Int, tags: String?): Flow<Resource<List<Recipe>>>

    /**
     * Finds recipes that can be made with the given ingredients.
     *
     * @param ingredients List of ingredient names
     * @param count Maximum number of results
     * @return Flow emitting Resource with matching recipes
     */
    fun getRecipesByIngredients(
        ingredients: List<String>,
        count: Int
    ): Flow<Resource<List<Recipe>>>

    /**
     * Gets similar recipes to a given recipe.
     *
     * @param recipeId The recipe to find similar recipes for
     * @param count Number of similar recipes to fetch
     * @return Flow emitting Resource with similar recipes
     */
    fun getSimilarRecipes(recipeId: Long, count: Int): Flow<Resource<List<Recipe>>>
}
