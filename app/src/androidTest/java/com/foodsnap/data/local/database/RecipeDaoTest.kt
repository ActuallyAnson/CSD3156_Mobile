package com.foodsnap.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.foodsnap.data.local.database.dao.RecipeDao
import com.foodsnap.data.local.database.entity.RecipeEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Instrumentation tests for RecipeDao.
 *
 * Tests database operations:
 * - Insert and retrieve recipes
 * - Search functionality
 * - Update operations
 * - Delete operations
 */
@RunWith(AndroidJUnit4::class)
class RecipeDaoTest {

    private lateinit var recipeDao: RecipeDao
    private lateinit var database: FoodSnapDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            FoodSnapDatabase::class.java
        ).allowMainThreadQueries().build()
        recipeDao = database.recipeDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndRetrieveRecipe() = runTest {
        // Arrange
        val recipe = createTestRecipeEntity(1, "Test Recipe")

        // Act
        recipeDao.insertRecipe(recipe)
        val retrieved = recipeDao.getRecipeById(1)

        // Assert
        assertNotNull(retrieved)
        assertEquals("Test Recipe", retrieved?.title)
    }

    @Test
    fun insertMultipleRecipes() = runTest {
        // Arrange
        val recipes = listOf(
            createTestRecipeEntity(1, "Recipe 1"),
            createTestRecipeEntity(2, "Recipe 2"),
            createTestRecipeEntity(3, "Recipe 3")
        )

        // Act
        recipeDao.insertRecipes(recipes)
        val all = recipeDao.searchRecipesList("%%")

        // Assert
        assertEquals(3, all.size)
    }

    @Test
    fun searchRecipesByTitle() = runTest {
        // Arrange
        val recipes = listOf(
            createTestRecipeEntity(1, "Pasta Carbonara"),
            createTestRecipeEntity(2, "Pasta Bolognese"),
            createTestRecipeEntity(3, "Chicken Tikka")
        )
        recipeDao.insertRecipes(recipes)

        // Act
        val results = recipeDao.searchRecipesList("%pasta%")

        // Assert
        assertEquals(2, results.size)
        results.forEach { recipe ->
            assert(recipe.title.contains("Pasta", ignoreCase = true))
        }
    }

    @Test
    fun getRecipeByIdReturnsNullForNonExistent() = runTest {
        // Act
        val result = recipeDao.getRecipeById(999)

        // Assert
        assertNull(result)
    }

    @Test
    fun updateRecipeModifiesExisting() = runTest {
        // Arrange
        val recipe = createTestRecipeEntity(1, "Original Title")
        recipeDao.insertRecipe(recipe)

        // Act
        val updated = recipe.copy(title = "Updated Title")
        recipeDao.updateRecipe(updated)
        val retrieved = recipeDao.getRecipeById(1)

        // Assert
        assertEquals("Updated Title", retrieved?.title)
    }

    @Test
    fun deleteRecipeRemovesFromDatabase() = runTest {
        // Arrange
        val recipe = createTestRecipeEntity(1, "To Delete")
        recipeDao.insertRecipe(recipe)

        // Act
        recipeDao.deleteRecipe(recipe.id)
        val retrieved = recipeDao.getRecipeById(1)

        // Assert
        assertNull(retrieved)
    }

    @Test
    fun searchWithEmptyQueryReturnsAll() = runTest {
        // Arrange
        val recipes = listOf(
            createTestRecipeEntity(1, "Recipe A"),
            createTestRecipeEntity(2, "Recipe B")
        )
        recipeDao.insertRecipes(recipes)

        // Act
        val results = recipeDao.searchRecipesList("%%")

        // Assert
        assertEquals(2, results.size)
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
