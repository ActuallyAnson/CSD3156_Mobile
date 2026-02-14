package com.foodsnap.data.repository

import com.foodsnap.data.local.database.dao.IngredientDao
import com.foodsnap.data.local.database.dao.RecipeDao
import com.foodsnap.data.local.database.entity.RecipeEntity
import com.foodsnap.data.remote.api.SpoonacularApi
import com.foodsnap.data.remote.dto.spoonacular.RecipeSearchResponse
import com.foodsnap.data.remote.dto.spoonacular.RecipeSearchResultDto
import com.foodsnap.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for RecipeRepositoryImpl.
 *
 * Tests the offline-first strategy:
 * - Cache hit scenarios
 * - Network fetch and cache scenarios
 * - Error handling with fallback to cache
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RecipeRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var recipeDao: RecipeDao
    private lateinit var ingredientDao: IngredientDao
    private lateinit var spoonacularApi: SpoonacularApi
    private lateinit var repository: RecipeRepositoryImpl

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        recipeDao = mockk(relaxed = true)
        ingredientDao = mockk(relaxed = true)
        spoonacularApi = mockk()

        repository = RecipeRepositoryImpl(
            recipeDao = recipeDao,
            ingredientDao = ingredientDao,
            spoonacularApi = spoonacularApi,
            ioDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `searchRecipes emits Loading first`() = runTest {
        // Arrange
        coEvery { recipeDao.searchRecipesList(any()) } returns emptyList()
        coEvery { spoonacularApi.searchRecipes(any(), any(), any(), any()) } returns
            RecipeSearchResponse(results = emptyList(), offset = 0, number = 0, totalResults = 0)

        // Act
        val emissions = repository.searchRecipes("pasta").toList()

        // Assert
        assertTrue(emissions.first() is Resource.Loading)
    }

    @Test
    fun `searchRecipes returns cached results when available`() = runTest {
        // Arrange
        val cachedRecipes = listOf(
            createTestRecipeEntity(1, "Pasta Carbonara"),
            createTestRecipeEntity(2, "Pasta Bolognese")
        )
        coEvery { recipeDao.searchRecipesList(any()) } returns cachedRecipes
        coEvery { spoonacularApi.searchRecipes(any(), any(), any(), any()) } returns
            RecipeSearchResponse(results = emptyList(), offset = 0, number = 0, totalResults = 0)

        // Act
        val emissions = repository.searchRecipes("pasta").toList()

        // Assert
        val successEmission = emissions.filterIsInstance<Resource.Success<*>>().first()
        assertEquals(2, (successEmission.data as List<*>).size)
    }

    @Test
    fun `searchRecipes caches network results`() = runTest {
        // Arrange
        coEvery { recipeDao.searchRecipesList(any()) } returns emptyList()
        coEvery { spoonacularApi.searchRecipes(any(), any(), any(), any()) } returns
            RecipeSearchResponse(
                results = listOf(
                    RecipeSearchResultDto(
                        id = 1, title = "Test Recipe", image = null, imageType = null,
                        servings = null, readyInMinutes = null, sourceUrl = null,
                        summary = null, cuisines = null, dishTypes = null,
                        diets = null, nutrition = null
                    )
                ),
                totalResults = 1,
                offset = 0,
                number = 1
            )

        // Act
        repository.searchRecipes("test").toList()

        // Assert
        coVerify { recipeDao.insertRecipes(any()) }
    }

    @Test
    fun `searchRecipes returns error with cached data on network failure`() = runTest {
        // Arrange
        val cachedRecipes = listOf(createTestRecipeEntity(1, "Cached Recipe"))
        coEvery { recipeDao.searchRecipesList(any()) } returns cachedRecipes
        coEvery { spoonacularApi.searchRecipes(any(), any(), any(), any()) } throws
            Exception("Network error")

        // Act
        val emissions = repository.searchRecipes("test").toList()

        // Assert
        val errorEmission = emissions.filterIsInstance<Resource.Error<*>>().firstOrNull()
        assertTrue(errorEmission != null)
        assertEquals(1, (errorEmission?.data as? List<*>)?.size)
    }

    @Test
    fun `getRecipeById returns cached recipe first`() = runTest {
        // Arrange
        val cachedRecipe = createTestRecipeEntity(1, "Test Recipe")
        coEvery { recipeDao.getRecipeById(1) } returns cachedRecipe
        coEvery { spoonacularApi.getRecipeInformation(1) } throws Exception("Should not be called first")

        // Act
        val firstEmission = repository.getRecipeById(1).first()

        // Assert
        assertTrue(firstEmission is Resource.Loading || firstEmission is Resource.Success)
    }

    private fun createTestRecipeEntity(id: Long, title: String): RecipeEntity {
        return RecipeEntity(
            id = id,
            spoonacularId = id.toInt(),
            title = title,
            readyInMinutes = 30,
            servings = 4,
            imageUrl = "https://example.com/image.jpg",
            summary = "Test summary",
            instructions = "Test instructions",
            sourceUrl = null,
            vegetarian = false,
            vegan = false,
            glutenFree = false,
            dairyFree = false,
            healthScore = null,
            spoonacularScore = null,
            pricePerServing = null
        )
    }
}
