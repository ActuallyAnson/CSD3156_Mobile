package com.foodsnap.domain.usecase.saved

import com.foodsnap.domain.model.Recipe
import com.foodsnap.domain.repository.SavedRecipeRepository
import com.foodsnap.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for saving a recipe to favorites.
 */
class SaveRecipeUseCase @Inject constructor(
    private val repository: SavedRecipeRepository
) {
    suspend operator fun invoke(recipeId: Long, notes: String? = null) {
        repository.saveRecipe(recipeId, notes)
    }
}

/**
 * Use case for removing a recipe from favorites.
 */
class RemoveSavedRecipeUseCase @Inject constructor(
    private val repository: SavedRecipeRepository
) {
    suspend operator fun invoke(recipeId: Long) {
        repository.removeSavedRecipe(recipeId)
    }
}

/**
 * Use case for getting all saved recipes.
 */
class GetSavedRecipesUseCase @Inject constructor(
    private val repository: SavedRecipeRepository
) {
    operator fun invoke(): Flow<Resource<List<Recipe>>> {
        return repository.getSavedRecipes()
    }
}

/**
 * Use case for checking if a recipe is saved.
 */
class IsRecipeSavedUseCase @Inject constructor(
    private val repository: SavedRecipeRepository
) {
    operator fun invoke(recipeId: Long): Flow<Boolean> {
        return repository.isRecipeSaved(recipeId)
    }
}
