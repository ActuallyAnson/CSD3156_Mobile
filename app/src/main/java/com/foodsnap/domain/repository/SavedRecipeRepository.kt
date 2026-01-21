package com.foodsnap.domain.repository

import com.foodsnap.domain.model.Recipe
import com.foodsnap.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for saved/favorite recipe operations.
 *
 * Handles CRUD operations for user's saved recipes.
 */
interface SavedRecipeRepository {

    /**
     * Gets all saved recipes.
     *
     * @return Flow emitting Resource with saved recipes
     */
    fun getSavedRecipes(): Flow<Resource<List<Recipe>>>

    /**
     * Saves a recipe to favorites.
     *
     * @param recipeId The recipe ID to save
     * @param notes Optional user notes
     */
    suspend fun saveRecipe(recipeId: Long, notes: String? = null)

    /**
     * Removes a recipe from favorites.
     *
     * @param recipeId The recipe ID to remove
     */
    suspend fun removeSavedRecipe(recipeId: Long)

    /**
     * Checks if a recipe is saved.
     *
     * @param recipeId The recipe ID to check
     * @return Flow emitting boolean indicating saved status
     */
    fun isRecipeSaved(recipeId: Long): Flow<Boolean>
}
