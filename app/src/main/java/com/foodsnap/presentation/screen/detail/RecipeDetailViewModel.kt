package com.foodsnap.presentation.screen.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodsnap.domain.model.Comment
import com.foodsnap.domain.model.Recipe
import com.foodsnap.domain.usecase.comment.AddCommentUseCase
import com.foodsnap.domain.usecase.comment.GetAverageRatingUseCase
import com.foodsnap.domain.usecase.comment.GetCommentsForRecipeUseCase
import com.foodsnap.domain.usecase.recipe.GetRecipeByIdUseCase
import com.foodsnap.domain.usecase.saved.IsRecipeSavedUseCase
import com.foodsnap.domain.usecase.saved.RemoveSavedRecipeUseCase
import com.foodsnap.domain.usecase.saved.SaveRecipeUseCase
import com.foodsnap.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the Recipe Detail screen.
 *
 * @property recipe The recipe being displayed
 * @property isSaved Whether the recipe is saved to favorites
 * @property comments List of comments for this recipe
 * @property averageRating Average rating from all comments
 * @property isLoading Whether data is being loaded
 * @property error Error message if any
 */
data class RecipeDetailUiState(
    val recipe: Recipe? = null,
    val isSaved: Boolean = false,
    val comments: List<Comment> = emptyList(),
    val averageRating: Float? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel for the Recipe Detail screen.
 *
 * Loads recipe details, manages saved state, and handles comments.
 *
 * @property savedStateHandle Provides access to navigation arguments
 * @property getRecipeByIdUseCase Use case for fetching recipe details
 * @property isRecipeSavedUseCase Use case for checking saved status
 * @property saveRecipeUseCase Use case for saving a recipe
 * @property removeSavedRecipeUseCase Use case for removing a saved recipe
 * @property getCommentsForRecipeUseCase Use case for fetching comments
 * @property addCommentUseCase Use case for adding a comment
 * @property getAverageRatingUseCase Use case for getting average rating
 */
@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRecipeByIdUseCase: GetRecipeByIdUseCase,
    private val isRecipeSavedUseCase: IsRecipeSavedUseCase,
    private val saveRecipeUseCase: SaveRecipeUseCase,
    private val removeSavedRecipeUseCase: RemoveSavedRecipeUseCase,
    private val getCommentsForRecipeUseCase: GetCommentsForRecipeUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val getAverageRatingUseCase: GetAverageRatingUseCase
) : ViewModel() {

    private val recipeId: Long = savedStateHandle.get<Long>("recipeId") ?: 0L

    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    init {
        loadRecipe()
        checkSavedStatus()
        loadComments()
        loadAverageRating()
    }

    /**
     * Loads the recipe details.
     */
    private fun loadRecipe() {
        viewModelScope.launch {
            getRecipeByIdUseCase(recipeId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                recipe = result.data,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message,
                                recipe = result.data
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if the recipe is saved to favorites.
     */
    private fun checkSavedStatus() {
        viewModelScope.launch {
            isRecipeSavedUseCase(recipeId).collect { isSaved ->
                _uiState.update { it.copy(isSaved = isSaved) }
            }
        }
    }

    /**
     * Loads comments for the recipe.
     */
    private fun loadComments() {
        viewModelScope.launch {
            getCommentsForRecipeUseCase(recipeId).collect { comments ->
                _uiState.update { it.copy(comments = comments) }
            }
        }
    }

    /**
     * Loads average rating for the recipe.
     */
    private fun loadAverageRating() {
        viewModelScope.launch {
            getAverageRatingUseCase(recipeId).collect { rating ->
                _uiState.update { it.copy(averageRating = rating) }
            }
        }
    }

    /**
     * Toggles the saved state of the recipe.
     */
    fun toggleSaved() {
        viewModelScope.launch {
            if (_uiState.value.isSaved) {
                removeSavedRecipeUseCase(recipeId)
            } else {
                saveRecipeUseCase(recipeId)
            }
        }
    }

    /**
     * Adds a comment to the recipe.
     *
     * @param text Comment text
     * @param rating Rating value (1-5)
     */
    fun addComment(text: String, rating: Int) {
        viewModelScope.launch {
            addCommentUseCase(
                recipeId = recipeId,
                text = text,
                rating = rating,
                userName = "User" // Simplified - would use actual user name
            )
        }
    }

    /**
     * Shares the recipe.
     */
    fun shareRecipe() {
        // TODO: Implement share functionality using Content Provider
    }
}
