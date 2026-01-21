package com.foodsnap.data.repository

import com.foodsnap.data.local.database.dao.IngredientDao
import com.foodsnap.data.local.database.dao.RecipeDao
import com.foodsnap.data.mapper.RecipeMapper
import com.foodsnap.data.remote.api.SpoonacularApi
import com.foodsnap.di.IoDispatcher
import com.foodsnap.domain.model.Recipe
import com.foodsnap.domain.repository.RecipeRepository
import com.foodsnap.util.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * Implementation of RecipeRepository.
 *
 * Follows offline-first strategy:
 * 1. Emit cached data if available
 * 2. Fetch from network
 * 3. Cache new data
 * 4. Emit updated data
 */
class RecipeRepositoryImpl @Inject constructor(
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao,
    private val spoonacularApi: SpoonacularApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RecipeRepository {

    override fun searchRecipes(
        query: String,
        cuisine: String?,
        diet: String?,
        type: String?
    ): Flow<Resource<List<Recipe>>> = flow {
        emit(Resource.Loading())

        try {
            // First, emit cached results if available
            val cachedRecipes = recipeDao.searchRecipesList("%$query%")
            if (cachedRecipes.isNotEmpty()) {
                emit(Resource.Success(cachedRecipes.map { RecipeMapper.toDomain(it) }))
            }

            // Fetch from network
            val response = spoonacularApi.searchRecipes(
                query = query,
                cuisine = cuisine,
                diet = diet,
                type = type
            )

            // Cache results
            val entities = response.results.map { RecipeMapper.toEntity(it) }
            recipeDao.insertRecipes(entities)

            // Emit fresh results
            emit(Resource.Success(entities.map { RecipeMapper.toDomain(it) }))

        } catch (e: Exception) {
            // On error, try to return cached data
            val cachedRecipes = recipeDao.searchRecipesList("%$query%")
            if (cachedRecipes.isNotEmpty()) {
                emit(Resource.Error(
                    message = e.message ?: "Network error",
                    data = cachedRecipes.map { RecipeMapper.toDomain(it) }
                ))
            } else {
                emit(Resource.Error(e.message ?: "Failed to search recipes"))
            }
        }
    }.flowOn(ioDispatcher)

    override fun getRecipeById(recipeId: Long): Flow<Resource<Recipe>> = flow {
        emit(Resource.Loading())

        try {
            // Check cache first
            val cachedRecipe = recipeDao.getRecipeById(recipeId)
            if (cachedRecipe != null) {
                emit(Resource.Success(RecipeMapper.toDomain(cachedRecipe)))
            }

            // Fetch full details from network
            val response = spoonacularApi.getRecipeInformation(recipeId.toInt())

            // Cache recipe
            val entity = RecipeMapper.toEntity(response)
            recipeDao.insertRecipe(entity)

            // Cache ingredients and relationships
            response.extendedIngredients?.forEach { ingredientDto ->
                val ingredientEntity = RecipeMapper.toIngredientEntity(ingredientDto)
                ingredientDao.insertIngredient(ingredientEntity)

                val crossRef = RecipeMapper.toRecipeIngredientCrossRef(recipeId, ingredientDto)
                ingredientDao.insertRecipeIngredientCrossRef(crossRef)
            }

            // Get full recipe with ingredients
            val recipeWithIngredients = recipeDao.getRecipeWithIngredients(recipeId)
            if (recipeWithIngredients != null) {
                val ingredientCrossRefs = ingredientDao.getRecipeIngredientCrossRefs(recipeId)
                val ingredientsWithAmount = recipeWithIngredients.ingredients.map { ingredient ->
                    val crossRef = ingredientCrossRefs.find { it.ingredientId == ingredient.id }
                    com.foodsnap.data.local.database.relation.IngredientWithAmount(
                        ingredient = ingredient,
                        amount = crossRef?.amount ?: 0.0,
                        unit = crossRef?.unit ?: "",
                        original = crossRef?.original ?: ""
                    )
                }
                emit(Resource.Success(RecipeMapper.toDomain(entity, ingredientsWithAmount)))
            } else {
                emit(Resource.Success(RecipeMapper.toDomain(entity)))
            }

        } catch (e: Exception) {
            val cachedRecipe = recipeDao.getRecipeById(recipeId)
            if (cachedRecipe != null) {
                emit(Resource.Error(
                    message = e.message ?: "Network error",
                    data = RecipeMapper.toDomain(cachedRecipe)
                ))
            } else {
                emit(Resource.Error(e.message ?: "Failed to load recipe"))
            }
        }
    }.flowOn(ioDispatcher)

    override fun getRandomRecipes(count: Int, tags: String?): Flow<Resource<List<Recipe>>> = flow {
        emit(Resource.Loading())

        try {
            // Fetch random recipes from network
            val response = spoonacularApi.getRandomRecipes(count, tags)

            // Cache results
            val entities = response.recipes.map { RecipeMapper.toEntity(it) }
            recipeDao.insertRecipes(entities)

            // Emit results
            emit(Resource.Success(entities.map { RecipeMapper.toDomain(it) }))

        } catch (e: Exception) {
            // On error, return any cached recipes
            val cachedRecipes = recipeDao.searchRecipesList("%%")
            if (cachedRecipes.isNotEmpty()) {
                emit(Resource.Error(
                    message = e.message ?: "Network error",
                    data = cachedRecipes.take(count).map { RecipeMapper.toDomain(it) }
                ))
            } else {
                emit(Resource.Error(e.message ?: "Failed to load recipes"))
            }
        }
    }.flowOn(ioDispatcher)

    override fun getRecipesByIngredients(
        ingredients: List<String>,
        count: Int
    ): Flow<Resource<List<Recipe>>> = flow {
        emit(Resource.Loading())

        try {
            // Fetch from network
            val ingredientsQuery = ingredients.joinToString(",")
            val response = spoonacularApi.findByIngredients(ingredientsQuery, count)

            // Cache results
            val entities = response.map { RecipeMapper.toEntity(it) }
            recipeDao.insertRecipes(entities)

            // Emit results
            emit(Resource.Success(entities.map { RecipeMapper.toDomain(it) }))

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to search by ingredients"))
        }
    }.flowOn(ioDispatcher)

    override fun getSimilarRecipes(recipeId: Long, count: Int): Flow<Resource<List<Recipe>>> = flow {
        emit(Resource.Loading())

        try {
            val response = spoonacularApi.getSimilarRecipes(recipeId.toInt(), count)

            // For similar recipes, we need to fetch full details
            val recipes = response.map { similarDto ->
                try {
                    val details = spoonacularApi.getRecipeInformation(similarDto.id)
                    RecipeMapper.toEntity(details)
                } catch (e: Exception) {
                    // If we can't get details, create minimal entity
                    com.foodsnap.data.local.database.entity.RecipeEntity(
                        id = similarDto.id.toLong(),
                        spoonacularId = similarDto.id,
                        title = similarDto.title,
                        readyInMinutes = similarDto.readyInMinutes ?: 0,
                        servings = similarDto.servings ?: 1
                    )
                }
            }

            recipeDao.insertRecipes(recipes)
            emit(Resource.Success(recipes.map { RecipeMapper.toDomain(it) }))

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load similar recipes"))
        }
    }.flowOn(ioDispatcher)
}
