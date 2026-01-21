package com.foodsnap.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing an ingredient in the user's inventory/pantry.
 *
 * Tracks what ingredients the user has available, including quantities
 * and expiration dates for meal planning and recipe suggestions.
 *
 * @property id Auto-generated primary key
 * @property ingredientId Optional foreign key to ingredient catalog
 * @property name Ingredient name (may differ from catalog for custom items)
 * @property quantity Amount available
 * @property unit Unit of measurement
 * @property expirationDate Expiration date as timestamp (null if not tracked)
 * @property addedAt Timestamp when added to inventory
 * @property location Storage location (fridge, pantry, freezer)
 */
@Entity(
    tableName = "user_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = IngredientEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["ingredientId"]),
        Index(value = ["name"]),
        Index(value = ["expirationDate"])
    ]
)
data class UserIngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ingredientId: Long? = null,
    val name: String,
    val quantity: Double,
    val unit: String,
    val expirationDate: Long? = null,
    val addedAt: Long = System.currentTimeMillis(),
    val location: String? = null
)
