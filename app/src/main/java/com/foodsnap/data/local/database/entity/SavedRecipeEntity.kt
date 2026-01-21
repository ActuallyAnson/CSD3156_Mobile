package com.foodsnap.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a user's saved/favorite recipe.
 *
 * Tracks which recipes the user has saved along with any personal notes.
 *
 * @property id Auto-generated primary key
 * @property recipeId Foreign key to the saved recipe
 * @property savedAt Timestamp when the recipe was saved
 * @property notes User's personal notes about the recipe
 * @property category User-defined category (e.g., "Weeknight Dinners")
 * @property personalRating User's personal rating (1-5)
 */
@Entity(
    tableName = "saved_recipes",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["recipeId"], unique = true)
    ]
)
data class SavedRecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipeId: Long,
    val savedAt: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val category: String? = null,
    val personalRating: Int? = null
)
