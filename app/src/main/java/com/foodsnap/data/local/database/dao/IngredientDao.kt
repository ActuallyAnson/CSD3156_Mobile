package com.foodsnap.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.foodsnap.data.local.database.entity.IngredientEntity
import com.foodsnap.data.local.database.entity.RecipeIngredientCrossRef
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Ingredient operations.
 *
 * Handles ingredient catalog CRUD and recipe-ingredient relationships.
 */
@Dao
interface IngredientDao {

    // ============== INSERT OPERATIONS ==============

    /**
     * Inserts a single ingredient, replacing on conflict.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: IngredientEntity)

    /**
     * Inserts multiple ingredients, replacing on conflict.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<IngredientEntity>)

    /**
     * Inserts a recipe-ingredient relationship.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredientCrossRef(crossRef: RecipeIngredientCrossRef)

    /**
     * Inserts multiple recipe-ingredient relationships.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredientCrossRefs(crossRefs: List<RecipeIngredientCrossRef>)

    // ============== UPDATE OPERATIONS ==============

    /**
     * Updates an ingredient.
     */
    @Update
    suspend fun updateIngredient(ingredient: IngredientEntity)

    // ============== DELETE OPERATIONS ==============

    /**
     * Deletes an ingredient by ID.
     */
    @Query("DELETE FROM ingredients WHERE id = :ingredientId")
    suspend fun deleteIngredient(ingredientId: Long)

    /**
     * Deletes all recipe-ingredient relationships for a recipe.
     */
    @Query("DELETE FROM recipe_ingredient_cross_ref WHERE recipeId = :recipeId")
    suspend fun deleteRecipeIngredients(recipeId: Long)

    /**
     * Clears all ingredients.
     */
    @Query("DELETE FROM ingredients")
    suspend fun clearAllIngredients()

    // ============== QUERY OPERATIONS ==============

    /**
     * Gets an ingredient by ID.
     */
    @Query("SELECT * FROM ingredients WHERE id = :ingredientId")
    suspend fun getIngredientById(ingredientId: Long): IngredientEntity?

    /**
     * Gets an ingredient by barcode.
     */
    @Query("SELECT * FROM ingredients WHERE barcode = :barcode")
    suspend fun getIngredientByBarcode(barcode: String): IngredientEntity?

    /**
     * Searches ingredients by name.
     */
    @Query("SELECT * FROM ingredients WHERE LOWER(name) LIKE LOWER(:query) ORDER BY name LIMIT :limit")
    suspend fun searchIngredients(query: String, limit: Int = 20): List<IngredientEntity>

    /**
     * Searches ingredients by name as a Flow.
     */
    @Query("SELECT * FROM ingredients WHERE LOWER(name) LIKE LOWER(:query) ORDER BY name")
    fun searchIngredientsFlow(query: String): Flow<List<IngredientEntity>>

    /**
     * Gets all ingredients.
     */
    @Query("SELECT * FROM ingredients ORDER BY name")
    fun getAllIngredients(): Flow<List<IngredientEntity>>

    /**
     * Gets ingredients for a specific recipe.
     */
    @Query("""
        SELECT i.* FROM ingredients i
        INNER JOIN recipe_ingredient_cross_ref ref ON i.id = ref.ingredientId
        WHERE ref.recipeId = :recipeId
    """)
    suspend fun getIngredientsForRecipe(recipeId: Long): List<IngredientEntity>

    /**
     * Gets the cross-reference data for a recipe (includes amounts).
     */
    @Query("SELECT * FROM recipe_ingredient_cross_ref WHERE recipeId = :recipeId")
    suspend fun getRecipeIngredientCrossRefs(recipeId: Long): List<RecipeIngredientCrossRef>

    /**
     * Finds recipes that use a specific ingredient.
     */
    @Query("""
        SELECT DISTINCT ref.recipeId FROM recipe_ingredient_cross_ref ref
        WHERE ref.ingredientId = :ingredientId
    """)
    suspend fun getRecipeIdsForIngredient(ingredientId: Long): List<Long>

    /**
     * Finds recipes that use all of the specified ingredients.
     * Returns recipe IDs that have ALL of the given ingredients.
     */
    @Query("""
        SELECT recipeId FROM recipe_ingredient_cross_ref
        WHERE ingredientId IN (:ingredientIds)
        GROUP BY recipeId
        HAVING COUNT(DISTINCT ingredientId) = :ingredientCount
    """)
    suspend fun getRecipeIdsWithAllIngredients(
        ingredientIds: List<Long>,
        ingredientCount: Int
    ): List<Long>

    /**
     * Finds recipes that use any of the specified ingredients.
     * Ordered by how many of the ingredients they use.
     */
    @Query("""
        SELECT recipeId, COUNT(DISTINCT ingredientId) as matchCount
        FROM recipe_ingredient_cross_ref
        WHERE ingredientId IN (:ingredientIds)
        GROUP BY recipeId
        ORDER BY matchCount DESC
    """)
    suspend fun getRecipeIdsWithAnyIngredients(ingredientIds: List<Long>): List<RecipeIngredientMatch>

    /**
     * Gets ingredient count.
     */
    @Query("SELECT COUNT(*) FROM ingredients")
    suspend fun getIngredientCount(): Int
}

/**
 * Helper class for recipe-ingredient match count.
 */
data class RecipeIngredientMatch(
    val recipeId: Long,
    val matchCount: Int
)
