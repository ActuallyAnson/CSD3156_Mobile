package com.foodsnap.domain.usecase.comment

import com.foodsnap.domain.model.Comment
import com.foodsnap.domain.repository.CommentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting comments for a recipe.
 */
class GetCommentsForRecipeUseCase @Inject constructor(
    private val repository: CommentRepository
) {
    operator fun invoke(recipeId: Long): Flow<List<Comment>> {
        return repository.getCommentsForRecipe(recipeId)
    }
}

/**
 * Use case for adding a comment to a recipe.
 */
class AddCommentUseCase @Inject constructor(
    private val repository: CommentRepository
) {
    suspend operator fun invoke(
        recipeId: Long,
        text: String,
        rating: Int,
        userName: String
    ) {
        repository.addComment(recipeId, text, rating, userName)
    }
}

/**
 * Use case for getting average rating for a recipe.
 */
class GetAverageRatingUseCase @Inject constructor(
    private val repository: CommentRepository
) {
    operator fun invoke(recipeId: Long): Flow<Float?> {
        return repository.getAverageRating(recipeId)
    }
}

/**
 * Use case for deleting a comment.
 */
class DeleteCommentUseCase @Inject constructor(
    private val repository: CommentRepository
) {
    suspend operator fun invoke(commentId: Long) {
        repository.deleteComment(commentId)
    }
}
