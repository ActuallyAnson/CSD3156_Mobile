package com.foodsnap.domain.usecase.recipe

import com.foodsnap.domain.model.Recipe
import com.foodsnap.domain.model.RecipeIngredient
import com.foodsnap.domain.repository.RecipeRepository
import com.foodsnap.util.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SearchRecipesUseCase.
 *
 * Tests:
 * - Search query handling
 * - Filter application (diet, cuisine, type)
 * - Empty query handling
 * - Error propagation
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchRecipesUseCaseTest {

    private lateinit var repository: RecipeRepository
    private lateinit var useCase: SearchRecipesUseCase

    private val testRecipes = listOf(
        createTestRecipe(1, "Pasta Carbonara"),
        createTestRecipe(2, "Pasta Bolognese")
    )

    @Before
    fun setup() {
        repository = mockk()
        useCase = SearchRecipesUseCase(repository)
    }

    @Test
    fun `invoke calls repository with correct parameters`() = runTest {
        // Arrange
        coEvery {
            repository.searchRecipes(
                query = "pasta",
                diet = "vegetarian",
                cuisine = "italian",
                type = "main course"
            )
        } returns flowOf(Resource.Success(testRecipes))

        // Act
        val emissions = useCase(
            query = "pasta",
            diet = "vegetarian",
            cuisine = "italian",
            type = "main course"
        ).toList()

        // Assert
        val successEmission = emissions.filterIsInstance<Resource.Success<List<Recipe>>>().first()
        assertEquals(2, successEmission.data?.size)
    }

    @Test
    fun `invoke with empty query returns results`() = runTest {
        // Arrange
        coEvery {
            repository.searchRecipes("", null, null, null)
        } returns flowOf(Resource.Success(testRecipes))

        // Act
        val emissions = useCase(query = "").toList()

        // Assert
        assertTrue(emissions.any { it is Resource.Success })
    }

    @Test
    fun `invoke propagates errors from repository`() = runTest {
        // Arrange
        coEvery {
            repository.searchRecipes(any(), any(), any(), any())
        } returns flowOf(Resource.Error("Network error"))

        // Act
        val emissions = useCase(query = "pasta").toList()

        // Assert
        val errorEmission = emissions.filterIsInstance<Resource.Error<List<Recipe>>>().first()
        assertEquals("Network error", errorEmission.message)
    }

    @Test
    fun `invoke emits loading state first`() = runTest {
        // Arrange
        coEvery {
            repository.searchRecipes(any(), any(), any(), any())
        } returns flowOf(
            Resource.Loading(),
            Resource.Success(testRecipes)
        )

        // Act
        val emissions = useCase(query = "pasta").toList()

        // Assert
        assertTrue(emissions.first() is Resource.Loading)
    }

    private fun createTestRecipe(id: Long, title: String): Recipe {
        return Recipe(
            id = id,
            title = title,
            imageUrl = "https://example.com/$id.jpg",
            readyInMinutes = 30,
            servings = 4,
            summary = "Test summary for $title",
            instructions = listOf("Step 1", "Step 2"),
            ingredients = listOf(
                RecipeIngredient(id = 1L, name = "Ingredient 1", amount = 1.0, unit = "cup")
            ),
            rating = 4.5f,
            sourceUrl = null
        )
    }
}
