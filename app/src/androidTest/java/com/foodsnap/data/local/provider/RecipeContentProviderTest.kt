package com.foodsnap.data.local.provider

import android.content.ContentResolver
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for RecipeContentProvider.
 *
 * Tests:
 * - Query operations for recipes, saved recipes, ingredients
 * - URI matching
 * - MIME type returns
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RecipeContentProviderTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private lateinit var contentResolver: ContentResolver

    @Before
    fun setup() {
        hiltRule.inject()
        val context = ApplicationProvider.getApplicationContext<Context>()
        contentResolver = context.contentResolver
    }

    @Test
    fun queryRecipesReturnsValidCursor() {
        // Act
        val cursor = contentResolver.query(
            RecipeContract.Recipes.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        // Assert
        assertNotNull(cursor)
        cursor?.close()
    }

    @Test
    fun queryIngredientsReturnsValidCursor() {
        // Act
        val cursor = contentResolver.query(
            RecipeContract.Ingredients.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        // Assert
        assertNotNull(cursor)
        cursor?.close()
    }

    @Test
    fun querySavedRecipesReturnsValidCursor() {
        // Act
        val cursor = contentResolver.query(
            RecipeContract.SavedRecipes.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        // Assert
        assertNotNull(cursor)
        cursor?.close()
    }

    @Test
    fun getTypeReturnsCorrectMimeForRecipes() {
        // Act
        val mimeType = contentResolver.getType(RecipeContract.Recipes.CONTENT_URI)

        // Assert
        assertEquals(RecipeContract.Recipes.CONTENT_TYPE, mimeType)
    }

    @Test
    fun getTypeReturnsCorrectMimeForSingleRecipe() {
        // Arrange
        val uri = RecipeContract.buildRecipeUri(1)

        // Act
        val mimeType = contentResolver.getType(uri)

        // Assert
        assertEquals(RecipeContract.Recipes.CONTENT_ITEM_TYPE, mimeType)
    }

    @Test
    fun getTypeReturnsCorrectMimeForIngredients() {
        // Act
        val mimeType = contentResolver.getType(RecipeContract.Ingredients.CONTENT_URI)

        // Assert
        assertEquals(RecipeContract.Ingredients.CONTENT_TYPE, mimeType)
    }

    @Test
    fun buildRecipeUriCreatesValidUri() {
        // Act
        val uri = RecipeContract.buildRecipeUri(123)

        // Assert
        assertEquals("content://com.foodsnap.provider/recipes/123", uri.toString())
    }

    @Test
    fun getIdFromUriExtractsCorrectId() {
        // Arrange
        val uri = RecipeContract.buildRecipeUri(456)

        // Act
        val id = RecipeContract.getIdFromUri(uri)

        // Assert
        assertEquals(456L, id)
    }

    @Test
    fun insertReturnsNull() {
        // Content provider is read-only
        // Act
        val result = contentResolver.insert(
            RecipeContract.Recipes.CONTENT_URI,
            null
        )

        // Assert - insert should return null for read-only provider
        assertEquals(null, result)
    }

    @Test
    fun deleteReturnsZero() {
        // Content provider is read-only
        // Act
        val result = contentResolver.delete(
            RecipeContract.Recipes.CONTENT_URI,
            null,
            null
        )

        // Assert - delete should return 0 for read-only provider
        assertEquals(0, result)
    }

    @Test
    fun updateReturnsZero() {
        // Content provider is read-only
        // Act
        val result = contentResolver.update(
            RecipeContract.Recipes.CONTENT_URI,
            null,
            null,
            null
        )

        // Assert - update should return 0 for read-only provider
        assertEquals(0, result)
    }

    @Test
    fun queryWithProjectionReturnsOnlyRequestedColumns() {
        // Arrange
        val projection = arrayOf(
            RecipeContract.Recipes.Columns.ID,
            RecipeContract.Recipes.Columns.TITLE
        )

        // Act
        val cursor = contentResolver.query(
            RecipeContract.Recipes.CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        // Assert
        assertNotNull(cursor)
        assertEquals(2, cursor?.columnCount)
        cursor?.close()
    }
}
