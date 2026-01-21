package com.foodsnap.domain.usecase.recipe

import com.foodsnap.domain.model.Recipe
import com.foodsnap.domain.repository.RecipeRepository
import com.foodsnap.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for finding recipes by available ingredients.
 *
 * Searches for recipes that can be made with the given ingredients.
 * Results are ranked by ingredient usage/coverage.
 *
 * @property repository The recipe repository
 */
class GetRecipesByIngredientsUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    /**
     * Finds recipes that use the given ingredients.
     *
     * @param ingredients List of ingredient names
     * @param count Maximum number of results
     * @return Flow of Resource containing matching recipes
     */
    operator fun invoke(
        ingredients: List<String>,
        count: Int = 20
    ): Flow<Resource<List<Recipe>>> {
        return repository.getRecipesByIngredients(ingredients, count)
    }
}
