package com.foodsnap.data.repository

import com.foodsnap.data.local.database.dao.CommentDao
import com.foodsnap.data.local.database.entity.CommentEntity
import com.foodsnap.di.IoDispatcher
import com.foodsnap.domain.model.Comment
import com.foodsnap.domain.repository.CommentRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of CommentRepository.
 *
 * Manages recipe comments and ratings in the local database.
 */
class CommentRepositoryImpl @Inject constructor(
    private val commentDao: CommentDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CommentRepository {

    override fun getCommentsForRecipe(recipeId: Long): Flow<List<Comment>> {
        return commentDao.getCommentsForRecipe(recipeId)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override suspend fun addComment(
        recipeId: Long,
        text: String,
        rating: Int,
        userName: String
    ) {
        // Generate a simple user ID based on device
        val userId = android.os.Build.DEVICE + "_" + android.os.Build.MODEL

        val entity = CommentEntity(
            recipeId = recipeId,
            userId = userId,
            userName = userName,
            text = text,
            rating = rating.coerceIn(1, 5),
            createdAt = System.currentTimeMillis()
        )
        commentDao.insertComment(entity)
    }

    override suspend fun deleteComment(commentId: Long) {
        commentDao.deleteComment(commentId)
    }

    override fun getAverageRating(recipeId: Long): Flow<Float?> {
        return commentDao.getAverageRating(recipeId)
            .flowOn(ioDispatcher)
    }

    /**
     * Extension function to convert entity to domain model.
     */
    private fun CommentEntity.toDomain(): Comment {
        return Comment(
            id = this.id,
            recipeId = this.recipeId,
            userId = this.userId,
            userName = this.userName,
            text = this.text,
            rating = this.rating,
            createdAt = this.createdAt
        )
    }
}
