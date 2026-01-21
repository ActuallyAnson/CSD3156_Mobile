package com.foodsnap.domain.usecase.recipe

import com.foodsnap.domain.model.Recipe
import com.foodsnap.domain.repository.RecipeRepository
import com.foodsnap.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting random/featured recipes.
 *
 * Returns a random selection of recipes for the home screen.
 *
 * @property repository The recipe repository
 */
class GetRandomRecipesUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    /**
     * Gets random recipes.
     *
     * @param count Number of recipes to return
     * @param tags Optional tags to filter by
     * @return Flow of Resource containing random recipes
     */
    operator fun invoke(
        count: Int = 20,
        tags: String? = null
    ): Flow<Resource<List<Recipe>>> {
        return repository.getRandomRecipes(count, tags)
    }
}
