package com.foodsnap.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Junction table for the many-to-many relationship between recipes and ingredients.
 *
 * Each row represents an ingredient used in a recipe with its quantity.
 *
 * @property recipeId Foreign key to recipes table
 * @property ingredientId Foreign key to ingredients table
 * @property amount Quantity of the ingredient needed
 * @property unit Unit of measurement (cups, grams, etc.)
 * @property original Original text description (e.g., "2 cups all-purpose flour")
 */
@Entity(
    tableName = "recipe_ingredient_cross_ref",
    primaryKeys = ["recipeId", "ingredientId"],
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = IngredientEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["ingredientId"]),
        Index(value = ["recipeId"])
    ]
)
data class RecipeIngredientCrossRef(
    val recipeId: Long,
    val ingredientId: Long,
    val amount: Double,
    val unit: String,
    val original: String = ""
)
