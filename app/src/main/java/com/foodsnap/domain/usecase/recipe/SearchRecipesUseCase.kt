package com.foodsnap.domain.usecase.recipe

import com.foodsnap.domain.model.Recipe
import com.foodsnap.domain.repository.RecipeRepository
import com.foodsnap.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for searching recipes by query.
 *
 * Searches recipes using text query with optional filters.
 * Results are returned as a Flow for reactive updates.
 *
 * @property repository The recipe repository
 */
class SearchRecipesUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    /**
     * Searches for recipes matching the query.
     *
     * @param query Search text
     * @param cuisine Optional cuisine filter
     * @param diet Optional diet filter
     * @param type Optional meal type filter
     * @return Flow of Resource containing matching recipes
     */
    operator fun invoke(
        query: String,
        cuisine: String? = null,
        diet: String? = null,
        type: String? = null
    ): Flow<Resource<List<Recipe>>> {
        return repository.searchRecipes(query, cuisine, diet, type)
    }
}
