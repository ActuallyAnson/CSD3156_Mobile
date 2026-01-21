package com.foodsnap.presentation.screen.saved

import com.foodsnap.domain.model.Recipe
import com.foodsnap.domain.model.Ingredient
import com.foodsnap.domain.usecase.saved.GetSavedRecipesUseCase
import com.foodsnap.domain.usecase.saved.RemoveSavedRecipeUseCase
import com.foodsnap.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SavedRecipesViewModel.
 *
 * Tests:
 * - Loading saved recipes
 * - Removing saved recipes
 * - Empty state handling
 * - Error handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SavedRecipesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getSavedRecipesUseCase: GetSavedRecipesUseCase
    private lateinit var removeSavedRecipeUseCase: RemoveSavedRecipeUseCase
    private lateinit var viewModel: SavedRecipesViewModel

    private val testRecipes = listOf(
        createTestRecipe(1, "Saved Recipe 1"),
        createTestRecipe(2, "Saved Recipe 2")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        getSavedRecipesUseCase = mockk()
        removeSavedRecipeUseCase = mockk()

        coEvery { getSavedRecipesUseCase() } returns flowOf(
            Resource.Success(testRecipes)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads saved recipes on init`() = runTest {
        // Act
        viewModel = SavedRecipesViewModel(getSavedRecipesUseCase, removeSavedRecipeUseCase)
        advanceUntilIdle()

        // Assert
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(2, viewModel.uiState.value.recipes.size)
    }

    @Test
    fun `shows empty state when no saved recipes`() = runTest {
        // Arrange
        coEvery { getSavedRecipesUseCase() } returns flowOf(
            Resource.Success(emptyList())
        )

        // Act
        viewModel = SavedRecipesViewModel(getSavedRecipesUseCase, removeSavedRecipeUseCase)
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.uiState.value.recipes.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `removeRecipe calls use case`() = runTest {
        // Arrange
        coEvery { removeSavedRecipeUseCase(any()) } returns flowOf(Resource.Success(Unit))

        viewModel = SavedRecipesViewModel(getSavedRecipesUseCase, removeSavedRecipeUseCase)
        advanceUntilIdle()

        // Act
        viewModel.removeRecipe(1L)
        advanceUntilIdle()

        // Assert
        coVerify { removeSavedRecipeUseCase(1L) }
    }

    @Test
    fun `error state is set on failure`() = runTest {
        // Arrange
        coEvery { getSavedRecipesUseCase() } returns flowOf(
            Resource.Error("Failed to load saved recipes")
        )

        // Act
        viewModel = SavedRecipesViewModel(getSavedRecipesUseCase, removeSavedRecipeUseCase)
        advanceUntilIdle()

        // Assert
        assertEquals("Failed to load saved recipes", viewModel.uiState.value.error)
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
