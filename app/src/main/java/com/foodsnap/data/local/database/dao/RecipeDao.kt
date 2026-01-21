package com.foodsnap.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.foodsnap.data.local.database.entity.RecipeEntity
import com.foodsnap.data.local.database.relation.RecipeWithComments
import com.foodsnap.data.local.database.relation.RecipeWithIngredients
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Recipe operations.
 *
 * Provides CRUD operations and complex queries for recipes.
 * All queries return Flow for reactive data updates.
 */
@Dao
interface RecipeDao {

    // ============== INSERT OPERATIONS ==============

    /**
     * Inserts a single recipe, replacing on conflict.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity)

    /**
     * Inserts multiple recipes, replacing on conflict.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<RecipeEntity>)

    // ============== UPDATE OPERATIONS ==============

    /**
     * Updates a recipe.
     */
    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    // ============== DELETE OPERATIONS ==============

    /**
     * Deletes a recipe by ID.
     */
    @Query("DELETE FROM recipes WHERE id = :recipeId")
    suspend fun deleteRecipe(recipeId: Long)

    /**
     * Deletes all cached recipes older than the given timestamp.
     */
    @Query("DELETE FROM recipes WHERE isCached = 1 AND createdAt < :timestamp")
    suspend fun deleteOldCachedRecipes(timestamp: Long)

    /**
     * Clears all recipes from the database.
     */
    @Query("DELETE FROM recipes")
    suspend fun clearAllRecipes()

    // ============== QUERY OPERATIONS ==============

    /**
     * Gets a recipe by ID.
     */
    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    suspend fun getRecipeById(recipeId: Long): RecipeEntity?

    /**
     * Gets a recipe by ID as a Flow for reactive updates.
     */
    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    fun getRecipeByIdFlow(recipeId: Long): Flow<RecipeEntity?>

    /**
     * Gets a recipe by Spoonacular ID.
     */
    @Query("SELECT * FROM recipes WHERE spoonacularId = :spoonacularId")
    suspend fun getRecipeBySpoonacularId(spoonacularId: Int): RecipeEntity?

    /**
     * Gets all recipes as a Flow.
     */
    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    /**
     * Searches recipes by title (case-insensitive).
     */
    @Query("SELECT * FROM recipes WHERE LOWER(title) LIKE LOWER(:query) ORDER BY title")
    fun searchRecipes(query: String): Flow<List<RecipeEntity>>

    /**
     * Searches recipes by title, returning a list (not Flow).
     */
    @Query("SELECT * FROM recipes WHERE LOWER(title) LIKE LOWER(:query) ORDER BY title")
    suspend fun searchRecipesList(query: String): List<RecipeEntity>

    /**
     * Gets recipes by cuisine type.
     */
    @Query("SELECT * FROM recipes WHERE cuisines LIKE '%' || :cuisine || '%'")
    fun getRecipesByCuisine(cuisine: String): Flow<List<RecipeEntity>>

    /**
     * Gets recipes by diet type.
     */
    @Query("SELECT * FROM recipes WHERE diets LIKE '%' || :diet || '%'")
    fun getRecipesByDiet(diet: String): Flow<List<RecipeEntity>>

    /**
     * Gets recipes that can be made within the given time.
     */
    @Query("SELECT * FROM recipes WHERE readyInMinutes <= :maxMinutes ORDER BY readyInMinutes")
    fun getQuickRecipes(maxMinutes: Int): Flow<List<RecipeEntity>>

    /**
     * Gets the count of cached recipes.
     */
    @Query("SELECT COUNT(*) FROM recipes WHERE isCached = 1")
    suspend fun getCachedRecipeCount(): Int

    // ============== RELATIONSHIP QUERIES ==============

    /**
     * Gets a recipe with all its ingredients.
     */
    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    suspend fun getRecipeWithIngredients(recipeId: Long): RecipeWithIngredients?

    /**
     * Gets a recipe with all its ingredients as a Flow.
     */
    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    fun getRecipeWithIngredientsFlow(recipeId: Long): Flow<RecipeWithIngredients?>

    /**
     * Gets a recipe with all its comments.
     */
    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    suspend fun getRecipeWithComments(recipeId: Long): RecipeWithComments?

    /**
     * Gets all recipes with their ingredients.
     */
    @Transaction
    @Query("SELECT * FROM recipes ORDER BY title")
    fun getAllRecipesWithIngredients(): Flow<List<RecipeWithIngredients>>
}
