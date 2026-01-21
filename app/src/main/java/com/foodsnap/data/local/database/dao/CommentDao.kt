package com.foodsnap.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.foodsnap.data.local.database.entity.CommentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Comment operations.
 *
 * Handles recipe reviews and ratings CRUD operations.
 */
@Dao
interface CommentDao {

    // ============== INSERT OPERATIONS ==============

    /**
     * Adds a comment to a recipe.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity): Long

    /**
     * Adds multiple comments.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<CommentEntity>)

    // ============== UPDATE OPERATIONS ==============

    /**
     * Updates a comment.
     */
    @Update
    suspend fun updateComment(comment: CommentEntity)

    // ============== DELETE OPERATIONS ==============

    /**
     * Deletes a comment by ID.
     */
    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteComment(commentId: Long)

    /**
     * Deletes all comments for a recipe.
     */
    @Query("DELETE FROM comments WHERE recipeId = :recipeId")
    suspend fun deleteCommentsForRecipe(recipeId: Long)

    /**
     * Deletes all comments by a user.
     */
    @Query("DELETE FROM comments WHERE userId = :userId")
    suspend fun deleteCommentsByUser(userId: String)

    /**
     * Clears all comments.
     */
    @Query("DELETE FROM comments")
    suspend fun clearAllComments()

    // ============== QUERY OPERATIONS ==============

    /**
     * Gets a comment by ID.
     */
    @Query("SELECT * FROM comments WHERE id = :commentId")
    suspend fun getCommentById(commentId: Long): CommentEntity?

    /**
     * Gets all comments for a recipe.
     */
    @Query("SELECT * FROM comments WHERE recipeId = :recipeId ORDER BY createdAt DESC")
    fun getCommentsForRecipe(recipeId: Long): Flow<List<CommentEntity>>

    /**
     * Gets all comments for a recipe (non-Flow version).
     */
    @Query("SELECT * FROM comments WHERE recipeId = :recipeId ORDER BY createdAt DESC")
    suspend fun getCommentsForRecipeList(recipeId: Long): List<CommentEntity>

    /**
     * Gets comments by a user.
     */
    @Query("SELECT * FROM comments WHERE userId = :userId ORDER BY createdAt DESC")
    fun getCommentsByUser(userId: String): Flow<List<CommentEntity>>

    /**
     * Gets the average rating for a recipe.
     */
    @Query("SELECT AVG(CAST(rating AS FLOAT)) FROM comments WHERE recipeId = :recipeId")
    fun getAverageRating(recipeId: Long): Flow<Float?>

    /**
     * Gets the average rating for a recipe (non-Flow version).
     */
    @Query("SELECT AVG(CAST(rating AS FLOAT)) FROM comments WHERE recipeId = :recipeId")
    suspend fun getAverageRatingSync(recipeId: Long): Float?

    /**
     * Gets the comment count for a recipe.
     */
    @Query("SELECT COUNT(*) FROM comments WHERE recipeId = :recipeId")
    suspend fun getCommentCount(recipeId: Long): Int

    /**
     * Gets the rating distribution for a recipe (count per star rating).
     */
    @Query("""
        SELECT rating, COUNT(*) as count
        FROM comments
        WHERE recipeId = :recipeId
        GROUP BY rating
        ORDER BY rating DESC
    """)
    suspend fun getRatingDistribution(recipeId: Long): List<RatingCount>

    /**
     * Gets recent comments across all recipes.
     */
    @Query("SELECT * FROM comments ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentComments(limit: Int): Flow<List<CommentEntity>>

    /**
     * Checks if a user has already commented on a recipe.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM comments WHERE recipeId = :recipeId AND userId = :userId)")
    suspend fun hasUserCommented(recipeId: Long, userId: String): Boolean
}

/**
 * Helper class for rating distribution.
 */
data class RatingCount(
    val rating: Int,
    val count: Int
)
