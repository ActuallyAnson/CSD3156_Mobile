package com.foodsnap.data.repository

import com.foodsnap.data.local.database.dao.RecipeDao
import com.foodsnap.data.local.database.dao.SavedRecipeDao
import com.foodsnap.data.local.database.entity.SavedRecipeEntity
import com.foodsnap.data.mapper.RecipeMapper
import com.foodsnap.di.IoDispatcher
import com.foodsnap.domain.model.Recipe
import com.foodsnap.domain.repository.SavedRecipeRepository
import com.foodsnap.util.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of SavedRecipeRepository.
 *
 * Manages user's favorite recipes in the local database.
 */
class SavedRecipeRepositoryImpl @Inject constructor(
    private val savedRecipeDao: SavedRecipeDao,
    private val recipeDao: RecipeDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SavedRecipeRepository {

    override fun getSavedRecipes(): Flow<Resource<List<Recipe>>> = flow {
        emit(Resource.Loading())

        try {
            savedRecipeDao.getSavedRecipesWithDetails()
                .collect { recipeEntities ->
                    val recipes = recipeEntities.map { RecipeMapper.toDomain(it) }
                    emit(Resource.Success(recipes))
                }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load saved recipes"))
        }
    }.flowOn(ioDispatcher)

    override suspend fun saveRecipe(recipeId: Long, notes: String?) {
        // Check if recipe exists in database
        val recipe = recipeDao.getRecipeById(recipeId)
        if (recipe == null) {
            throw IllegalStateException("Recipe not found in database")
        }

        val savedRecipe = SavedRecipeEntity(
            recipeId = recipeId,
            notes = notes,
            savedAt = System.currentTimeMillis()
        )
        savedRecipeDao.insertSavedRecipe(savedRecipe)
    }

    override suspend fun removeSavedRecipe(recipeId: Long) {
        savedRecipeDao.deleteSavedRecipe(recipeId)
    }

    override fun isRecipeSaved(recipeId: Long): Flow<Boolean> {
        return savedRecipeDao.isRecipeSaved(recipeId)
    }
}
