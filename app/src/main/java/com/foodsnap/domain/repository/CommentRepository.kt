package com.foodsnap.domain.repository

import com.foodsnap.domain.model.Comment
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for recipe comment operations.
 *
 * Handles CRUD operations for recipe comments/reviews.
 */
interface CommentRepository {

    /**
     * Gets all comments for a recipe.
     *
     * @param recipeId The recipe ID
     * @return Flow emitting list of comments
     */
    fun getCommentsForRecipe(recipeId: Long): Flow<List<Comment>>

    /**
     * Adds a comment to a recipe.
     *
     * @param recipeId The recipe ID
     * @param text Comment content
     * @param rating Rating (1-5)
     * @param userName Display name
     */
    suspend fun addComment(
        recipeId: Long,
        text: String,
        rating: Int,
        userName: String
    )

    /**
     * Deletes a comment.
     *
     * @param commentId The comment ID to delete
     */
    suspend fun deleteComment(commentId: Long)

    /**
     * Gets the average rating for a recipe.
     *
     * @param recipeId The recipe ID
     * @return Flow emitting average rating or null if no ratings
     */
    fun getAverageRating(recipeId: Long): Flow<Float?>
}
