package com.foodsnap.domain.model

/**
 * Domain model representing an ingredient in the user's inventory/pantry.
 *
 * Tracks what ingredients the user has available, including quantities and expiration dates.
 *
 * @property id Unique identifier
 * @property ingredientId Reference to the base ingredient (if linked)
 * @property name Ingredient name
 * @property quantity Amount available
 * @property unit Unit of measurement
 * @property expirationDate Expiration date as timestamp (null if not tracked)
 * @property addedAt When the ingredient was added to inventory
 * @property location Storage location (fridge, pantry, freezer)
 */
data class UserIngredient(
    val id: Long,
    val ingredientId: Long? = null,
    val name: String,
    val quantity: Double,
    val unit: String,
    val expirationDate: Long? = null,
    val addedAt: Long = System.currentTimeMillis(),
    val location: String? = null
)
