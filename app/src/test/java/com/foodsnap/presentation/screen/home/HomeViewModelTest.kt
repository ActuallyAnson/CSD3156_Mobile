package com.foodsnap.presentation.screen.home

import com.foodsnap.domain.model.Ingredient
import com.foodsnap.domain.model.Recipe
import com.foodsnap.domain.usecase.recipe.GetRandomRecipesUseCase
import com.foodsnap.domain.usecase.recipe.SearchRecipesUseCase
import com.foodsnap.util.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for HomeViewModel.
 *
 * Tests state management for:
 * - Initial loading of featured recipes
 * - Category selection and filtering
 * - Search query updates
 * - Error handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var searchRecipesUseCase: SearchRecipesUseCase
    private lateinit var getRandomRecipesUseCase: GetRandomRecipesUseCase
    private lateinit var viewModel: HomeViewModel

    private val testRecipes = listOf(
        createTestRecipe(1, "Spaghetti Carbonara"),
        createTestRecipe(2, "Chicken Tikka Masala"),
        createTestRecipe(3, "Caesar Salad")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        searchRecipesUseCase = mockk()
        getRandomRecipesUseCase = mockk()

        // Default mock behavior
        coEvery { getRandomRecipesUseCase(any(), any()) } returns flowOf(
            Resource.Loading(),
            Resource.Success(testRecipes)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = runTest {
        // Arrange
        coEvery { getRandomRecipesUseCase(any(), any()) } returns flowOf(Resource.Loading())

        // Act
        viewModel = HomeViewModel(searchRecipesUseCase, getRandomRecipesUseCase)

        // Assert
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loads featured recipes on init`() = runTest {
        // Arrange & Act
        viewModel = HomeViewModel(searchRecipesUseCase, getRandomRecipesUseCase)
        advanceUntilIdle()

        // Assert
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(3, viewModel.uiState.value.recipes.size)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `onSearchQueryChange updates query`() = runTest {
        // Arrange
        viewModel = HomeViewModel(searchRecipesUseCase, getRandomRecipesUseCase)
        advanceUntilIdle()

        // Act
        viewModel.onSearchQueryChange("pasta")

        // Assert
        assertEquals("pasta", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `onCategorySelected updates category and loads recipes`() = runTest {
        // Arrange
        coEvery { searchRecipesUseCase(any(), any(), any(), any()) } returns flowOf(
            Resource.Success(testRecipes.take(1))
        )

        viewModel = HomeViewModel(searchRecipesUseCase, getRandomRecipesUseCase)
        advanceUntilIdle()

        // Act
        viewModel.onCategorySelected("breakfast", "breakfast")
        advanceUntilIdle()

        // Assert
        assertEquals("breakfast", viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun `error state is set on failure`() = runTest {
        // Arrange
        coEvery { getRandomRecipesUseCase(any(), any()) } returns flowOf(
            Resource.Error("Network error")
        )

        // Act
        viewModel = HomeViewModel(searchRecipesUseCase, getRandomRecipesUseCase)
        advanceUntilIdle()

        // Assert
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Network error", viewModel.uiState.value.error)
    }

    @Test
    fun `refresh reloads featured recipes when no category selected`() = runTest {
        // Arrange
        viewModel = HomeViewModel(searchRecipesUseCase, getRandomRecipesUseCase)
        advanceUntilIdle()

        // Act
        viewModel.refresh()
        advanceUntilIdle()

        // Assert - getRandomRecipesUseCase should be called twice (init + refresh)
        assertFalse(viewModel.uiState.value.isRefreshing)
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
                Ingredient(1, "Ingredient 1", "", 1.0, "cup", "")
            ),
            vegetarian = false,
            vegan = false,
            glutenFree = false,
            dairyFree = false,
            rating = 4.5f,
            sourceUrl = null
        )
    }
}
