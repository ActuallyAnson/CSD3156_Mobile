package com.foodsnap.domain.repository

import com.foodsnap.domain.model.UserIngredient
import com.foodsnap.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user inventory/pantry operations.
 *
 * Handles CRUD operations for user's ingredient inventory.
 */
interface UserInventoryRepository {

    /**
     * Gets all ingredients in the user's inventory.
     *
     * @return Flow emitting Resource with user ingredients
     */
    fun getUserInventory(): Flow<Resource<List<UserIngredient>>>

    /**
     * Adds an ingredient to the inventory.
     *
     * @param name Ingredient name
     * @param quantity Amount
     * @param unit Unit of measurement
     * @param expirationDate Optional expiration date
     * @param ingredientId Optional link to ingredient catalog
     */
    suspend fun addIngredient(
        name: String,
        quantity: Double,
        unit: String,
        expirationDate: Long? = null,
        ingredientId: Long? = null
    )

    /**
     * Removes an ingredient from the inventory.
     *
     * @param ingredientId The user ingredient ID to remove
     */
    suspend fun removeIngredient(ingredientId: Long)

    /**
     * Updates the quantity of an ingredient.
     *
     * @param ingredientId The user ingredient ID
     * @param newQuantity The new quantity
     */
    suspend fun updateQuantity(ingredientId: Long, newQuantity: Double)

    /**
     * Gets ingredients that are expiring soon.
     *
     * @param withinDays Number of days to check
     * @return Flow emitting list of expiring ingredients
     */
    fun getExpiringIngredients(withinDays: Int): Flow<List<UserIngredient>>
}
