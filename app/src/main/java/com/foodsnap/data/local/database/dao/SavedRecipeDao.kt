package com.foodsnap.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.foodsnap.data.local.database.entity.RecipeEntity
import com.foodsnap.data.local.database.entity.SavedRecipeEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Saved Recipe operations.
 *
 * Handles user's favorite recipes CRUD operations.
 */
@Dao
interface SavedRecipeDao {

    // ============== INSERT OPERATIONS ==============

    /**
     * Saves a recipe to favorites.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedRecipe(savedRecipe: SavedRecipeEntity)

    // ============== UPDATE OPERATIONS ==============

    /**
     * Updates a saved recipe (notes, category, rating).
     */
    @Update
    suspend fun updateSavedRecipe(savedRecipe: SavedRecipeEntity)

    /**
     * Updates notes for a saved recipe.
     */
    @Query("UPDATE saved_recipes SET notes = :notes WHERE recipeId = :recipeId")
    suspend fun updateNotes(recipeId: Long, notes: String?)

    /**
     * Updates personal rating for a saved recipe.
     */
    @Query("UPDATE saved_recipes SET personalRating = :rating WHERE recipeId = :recipeId")
    suspend fun updateRating(recipeId: Long, rating: Int?)

    // ============== DELETE OPERATIONS ==============

    /**
     * Removes a recipe from favorites by recipe ID.
     */
    @Query("DELETE FROM saved_recipes WHERE recipeId = :recipeId")
    suspend fun deleteSavedRecipe(recipeId: Long)

    /**
     * Clears all saved recipes.
     */
    @Query("DELETE FROM saved_recipes")
    suspend fun clearAllSavedRecipes()

    // ============== QUERY OPERATIONS ==============

    /**
     * Gets a saved recipe entry by recipe ID.
     */
    @Query("SELECT * FROM saved_recipes WHERE recipeId = :recipeId")
    suspend fun getSavedRecipeByRecipeId(recipeId: Long): SavedRecipeEntity?

    /**
     * Checks if a recipe is saved.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM saved_recipes WHERE recipeId = :recipeId)")
    fun isRecipeSaved(recipeId: Long): Flow<Boolean>

    /**
     * Checks if a recipe is saved (non-Flow version).
     */
    @Query("SELECT EXISTS(SELECT 1 FROM saved_recipes WHERE recipeId = :recipeId)")
    suspend fun isRecipeSavedSync(recipeId: Long): Boolean

    /**
     * Gets all saved recipe entries.
     */
    @Query("SELECT * FROM saved_recipes ORDER BY savedAt DESC")
    fun getAllSavedRecipes(): Flow<List<SavedRecipeEntity>>

    /**
     * Gets all saved recipes with full recipe data.
     */
    @Transaction
    @Query("""
        SELECT r.* FROM recipes r
        INNER JOIN saved_recipes sr ON r.id = sr.recipeId
        ORDER BY sr.savedAt DESC
    """)
    fun getSavedRecipesWithDetails(): Flow<List<RecipeEntity>>

    /**
     * Gets saved recipes by category.
     */
    @Transaction
    @Query("""
        SELECT r.* FROM recipes r
        INNER JOIN saved_recipes sr ON r.id = sr.recipeId
        WHERE sr.category = :category
        ORDER BY sr.savedAt DESC
    """)
    fun getSavedRecipesByCategory(category: String): Flow<List<RecipeEntity>>

    /**
     * Gets all unique categories used in saved recipes.
     */
    @Query("SELECT DISTINCT category FROM saved_recipes WHERE category IS NOT NULL")
    fun getAllCategories(): Flow<List<String>>

    /**
     * Gets the count of saved recipes.
     */
    @Query("SELECT COUNT(*) FROM saved_recipes")
    suspend fun getSavedRecipeCount(): Int

    /**
     * Gets all saved recipe IDs.
     */
    @Query("SELECT recipeId FROM saved_recipes ORDER BY savedAt DESC")
    fun getAllSavedRecipeIds(): Flow<List<Long>>

    /**
     * Gets saved recipes sorted by personal rating.
     */
    @Transaction
    @Query("""
        SELECT r.* FROM recipes r
        INNER JOIN saved_recipes sr ON r.id = sr.recipeId
        WHERE sr.personalRating IS NOT NULL
        ORDER BY sr.personalRating DESC
    """)
    fun getSavedRecipesByRating(): Flow<List<RecipeEntity>>
}
