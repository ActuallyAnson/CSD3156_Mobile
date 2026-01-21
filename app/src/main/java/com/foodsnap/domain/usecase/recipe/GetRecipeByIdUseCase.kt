package com.foodsnap.domain.usecase.recipe

import com.foodsnap.domain.model.Recipe
import com.foodsnap.domain.repository.RecipeRepository
import com.foodsnap.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting a single recipe by ID.
 *
 * @property repository The recipe repository
 */
class GetRecipeByIdUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    /**
     * Gets a recipe by its ID.
     *
     * @param recipeId The recipe ID
     * @return Flow of Resource containing the recipe
     */
    operator fun invoke(recipeId: Long): Flow<Resource<Recipe>> {
        return repository.getRecipeById(recipeId)
    }
}
