package com.foodsnap.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a user comment/review on a recipe.
 *
 * Stores user-generated reviews and ratings for recipes.
 *
 * @property id Auto-generated primary key
 * @property recipeId Foreign key to the recipe being reviewed
 * @property userId User identifier (device ID for local-only comments)
 * @property userName Display name of the reviewer
 * @property text Review content
 * @property rating Star rating (1-5)
 * @property createdAt Timestamp when the comment was created
 */
@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["recipeId"]),
        Index(value = ["userId"]),
        Index(value = ["createdAt"])
    ]
)
data class CommentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipeId: Long,
    val userId: String,
    val userName: String,
    val text: String,
    val rating: Int,
    val createdAt: Long = System.currentTimeMillis()
)
