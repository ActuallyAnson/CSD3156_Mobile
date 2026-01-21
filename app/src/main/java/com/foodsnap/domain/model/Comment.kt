package com.foodsnap.domain.model

/**
 * Domain model representing a user comment/review on a recipe.
 *
 * @property id Unique identifier
 * @property recipeId ID of the recipe this comment belongs to
 * @property userId User identifier (device ID for local comments)
 * @property userName Display name of the commenter
 * @property text Comment content
 * @property rating Rating given (1-5 stars)
 * @property createdAt Timestamp when comment was created
 */
data class Comment(
    val id: Long,
    val recipeId: Long,
    val userId: String,
    val userName: String,
    val text: String,
    val rating: Int,
    val createdAt: Long = System.currentTimeMillis()
)
