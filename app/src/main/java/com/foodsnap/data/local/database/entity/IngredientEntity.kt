package com.foodsnap.data.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing an ingredient in the catalog.
 *
 * This is the master list of ingredients that can be referenced
 * by recipes and user inventory.
 *
 * @property id Unique identifier
 * @property name Ingredient name
 * @property image URL or path to ingredient image
 * @property aisle Grocery store aisle/category
 * @property barcode Product barcode (for packaged items)
 * @property possibleUnits JSON array of valid measurement units
 */
@Entity(
    tableName = "ingredients",
    indices = [
        Index(value = ["name"]),
        Index(value = ["barcode"], unique = true)
    ]
)
data class IngredientEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val image: String? = null,
    val aisle: String? = null,
    val barcode: String? = null,
    val possibleUnits: String = "[]"
)
