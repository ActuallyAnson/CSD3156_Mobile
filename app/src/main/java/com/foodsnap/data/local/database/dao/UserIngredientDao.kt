package com.foodsnap.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.foodsnap.data.local.database.entity.UserIngredientEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for User Ingredient (Inventory) operations.
 *
 * Handles user's pantry/inventory CRUD operations and expiration tracking.
 */
@Dao
interface UserIngredientDao {

    // ============== INSERT OPERATIONS ==============

    /**
     * Adds an ingredient to the user's inventory.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserIngredient(ingredient: UserIngredientEntity): Long

    /**
     * Adds multiple ingredients to the user's inventory.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserIngredients(ingredients: List<UserIngredientEntity>)

    // ============== UPDATE OPERATIONS ==============

    /**
     * Updates a user ingredient.
     */
    @Update
    suspend fun updateUserIngredient(ingredient: UserIngredientEntity)

    /**
     * Updates the quantity of an ingredient.
     */
    @Query("UPDATE user_ingredients SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: Long, quantity: Double)

    /**
     * Updates the expiration date of an ingredient.
     */
    @Query("UPDATE user_ingredients SET expirationDate = :expirationDate WHERE id = :id")
    suspend fun updateExpirationDate(id: Long, expirationDate: Long?)

    // ============== DELETE OPERATIONS ==============

    /**
     * Removes an ingredient from the user's inventory.
     */
    @Query("DELETE FROM user_ingredients WHERE id = :id")
    suspend fun deleteUserIngredient(id: Long)

    /**
     * Removes all expired ingredients.
     */
    @Query("DELETE FROM user_ingredients WHERE expirationDate IS NOT NULL AND expirationDate < :currentTime")
    suspend fun deleteExpiredIngredients(currentTime: Long)

    /**
     * Clears all user ingredients.
     */
    @Query("DELETE FROM user_ingredients")
    suspend fun clearAllUserIngredients()

    // ============== QUERY OPERATIONS ==============

    /**
     * Gets a user ingredient by ID.
     */
    @Query("SELECT * FROM user_ingredients WHERE id = :id")
    suspend fun getUserIngredientById(id: Long): UserIngredientEntity?

    /**
     * Gets all user ingredients.
     */
    @Query("SELECT * FROM user_ingredients ORDER BY name")
    fun getAllUserIngredients(): Flow<List<UserIngredientEntity>>

    /**
     * Gets all user ingredients (non-Flow version).
     */
    @Query("SELECT * FROM user_ingredients ORDER BY name")
    suspend fun getAllUserIngredientsList(): List<UserIngredientEntity>

    /**
     * Gets user ingredients by location.
     */
    @Query("SELECT * FROM user_ingredients WHERE location = :location ORDER BY name")
    fun getUserIngredientsByLocation(location: String): Flow<List<UserIngredientEntity>>

    /**
     * Gets ingredients expiring within N days.
     */
    @Query("""
        SELECT * FROM user_ingredients
        WHERE expirationDate IS NOT NULL
        AND expirationDate > :currentTime
        AND expirationDate <= :futureTime
        ORDER BY expirationDate ASC
    """)
    fun getExpiringIngredients(currentTime: Long, futureTime: Long): Flow<List<UserIngredientEntity>>

    /**
     * Gets expired ingredients.
     */
    @Query("""
        SELECT * FROM user_ingredients
        WHERE expirationDate IS NOT NULL
        AND expirationDate < :currentTime
        ORDER BY expirationDate ASC
    """)
    fun getExpiredIngredients(currentTime: Long): Flow<List<UserIngredientEntity>>

    /**
     * Searches user ingredients by name.
     */
    @Query("SELECT * FROM user_ingredients WHERE LOWER(name) LIKE LOWER(:query) ORDER BY name")
    fun searchUserIngredients(query: String): Flow<List<UserIngredientEntity>>

    /**
     * Gets the count of user ingredients.
     */
    @Query("SELECT COUNT(*) FROM user_ingredients")
    suspend fun getUserIngredientCount(): Int

    /**
     * Gets the count of expiring ingredients (within N days).
     */
    @Query("""
        SELECT COUNT(*) FROM user_ingredients
        WHERE expirationDate IS NOT NULL
        AND expirationDate > :currentTime
        AND expirationDate <= :futureTime
    """)
    suspend fun getExpiringIngredientCount(currentTime: Long, futureTime: Long): Int

    /**
     * Gets all ingredient names for recipe matching.
     */
    @Query("SELECT name FROM user_ingredients")
    suspend fun getAllIngredientNames(): List<String>
}
