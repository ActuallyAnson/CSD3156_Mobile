package com.foodsnap.data.local.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.foodsnap.data.local.database.FoodSnapDatabase
import com.foodsnap.data.local.database.dao.RecipeDao
import com.foodsnap.data.local.database.dao.SavedRecipeDao
import com.foodsnap.data.local.database.dao.IngredientDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Content Provider for sharing recipe data with external applications.
 *
 * Provides read-only access to:
 * - Recipes (all and by ID)
 * - Saved recipes
 * - Ingredients
 *
 * External apps can query this provider to get recipe information
 * for display in widgets, assistant apps, or other integrations.
 */
class RecipeContentProvider : ContentProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface RecipeContentProviderEntryPoint {
        fun recipeDao(): RecipeDao
        fun savedRecipeDao(): SavedRecipeDao
        fun ingredientDao(): IngredientDao
    }

    private lateinit var recipeDao: RecipeDao
    private lateinit var savedRecipeDao: SavedRecipeDao
    private lateinit var ingredientDao: IngredientDao

    companion object {
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(
                RecipeContract.AUTHORITY,
                RecipeContract.Paths.RECIPES,
                RecipeContract.UriCodes.RECIPES
            )
            addURI(
                RecipeContract.AUTHORITY,
                "${RecipeContract.Paths.RECIPES}/#",
                RecipeContract.UriCodes.RECIPE_ID
            )
            addURI(
                RecipeContract.AUTHORITY,
                RecipeContract.Paths.SAVED_RECIPES,
                RecipeContract.UriCodes.SAVED_RECIPES
            )
            addURI(
                RecipeContract.AUTHORITY,
                "${RecipeContract.Paths.SAVED_RECIPES}/#",
                RecipeContract.UriCodes.SAVED_RECIPE_ID
            )
            addURI(
                RecipeContract.AUTHORITY,
                RecipeContract.Paths.INGREDIENTS,
                RecipeContract.UriCodes.INGREDIENTS
            )
            addURI(
                RecipeContract.AUTHORITY,
                "${RecipeContract.Paths.INGREDIENTS}/#",
                RecipeContract.UriCodes.INGREDIENT_ID
            )
        }
    }

    override fun onCreate(): Boolean {
        val appContext = context?.applicationContext ?: return false
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            RecipeContentProviderEntryPoint::class.java
        )
        recipeDao = entryPoint.recipeDao()
        savedRecipeDao = entryPoint.savedRecipeDao()
        ingredientDao = entryPoint.ingredientDao()
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return when (uriMatcher.match(uri)) {
            RecipeContract.UriCodes.RECIPES -> queryAllRecipes(projection, sortOrder)
            RecipeContract.UriCodes.RECIPE_ID -> queryRecipeById(uri, projection)
            RecipeContract.UriCodes.SAVED_RECIPES -> querySavedRecipes(projection, sortOrder)
            RecipeContract.UriCodes.INGREDIENTS -> queryAllIngredients(projection, sortOrder)
            else -> null
        }?.also { cursor ->
            cursor.setNotificationUri(context?.contentResolver, uri)
        }
    }

    private fun queryAllRecipes(projection: Array<out String>?, sortOrder: String?): Cursor {
        val cursor = MatrixCursor(
            projection ?: arrayOf(
                RecipeContract.Recipes.Columns.ID,
                RecipeContract.Recipes.Columns.TITLE,
                RecipeContract.Recipes.Columns.IMAGE_URL,
                RecipeContract.Recipes.Columns.READY_IN_MINUTES,
                RecipeContract.Recipes.Columns.SERVINGS
            )
        )

        runBlocking {
            val recipes = recipeDao.searchRecipesList("%%")
            recipes.forEach { recipe ->
                cursor.addRow(arrayOf(
                    recipe.id,
                    recipe.title,
                    recipe.imageUrl,
                    recipe.readyInMinutes,
                    recipe.servings
                ))
            }
        }

        return cursor
    }

    private fun queryRecipeById(uri: Uri, projection: Array<out String>?): Cursor {
        val id = RecipeContract.getIdFromUri(uri)
        val cursor = MatrixCursor(
            projection ?: arrayOf(
                RecipeContract.Recipes.Columns.ID,
                RecipeContract.Recipes.Columns.TITLE,
                RecipeContract.Recipes.Columns.IMAGE_URL,
                RecipeContract.Recipes.Columns.READY_IN_MINUTES,
                RecipeContract.Recipes.Columns.SERVINGS,
                RecipeContract.Recipes.Columns.SUMMARY,
                RecipeContract.Recipes.Columns.INSTRUCTIONS,
                RecipeContract.Recipes.Columns.VEGETARIAN,
                RecipeContract.Recipes.Columns.VEGAN,
                RecipeContract.Recipes.Columns.GLUTEN_FREE,
                RecipeContract.Recipes.Columns.DAIRY_FREE
            )
        )

        runBlocking {
            recipeDao.getRecipeById(id)?.let { recipe ->
                cursor.addRow(arrayOf(
                    recipe.id,
                    recipe.title,
                    recipe.imageUrl,
                    recipe.readyInMinutes,
                    recipe.servings,
                    recipe.summary,
                    recipe.instructions,
                    if (recipe.vegetarian) 1 else 0,
                    if (recipe.vegan) 1 else 0,
                    if (recipe.glutenFree) 1 else 0,
                    if (recipe.dairyFree) 1 else 0
                ))
            }
        }

        return cursor
    }

    private fun querySavedRecipes(projection: Array<out String>?, sortOrder: String?): Cursor {
        val cursor = MatrixCursor(
            projection ?: arrayOf(
                RecipeContract.SavedRecipes.Columns.ID,
                RecipeContract.SavedRecipes.Columns.RECIPE_ID,
                RecipeContract.SavedRecipes.Columns.NOTES,
                RecipeContract.SavedRecipes.Columns.SAVED_AT
            )
        )

        runBlocking {
            val savedRecipeIds = savedRecipeDao.getAllSavedRecipeIds().first()
            savedRecipeIds.forEach { savedRecipeId ->
                cursor.addRow(arrayOf(
                    savedRecipeId,
                    savedRecipeId,
                    null,
                    System.currentTimeMillis()
                ))
            }
        }

        return cursor
    }

    private fun queryAllIngredients(projection: Array<out String>?, sortOrder: String?): Cursor {
        val cursor = MatrixCursor(
            projection ?: arrayOf(
                RecipeContract.Ingredients.Columns.ID,
                RecipeContract.Ingredients.Columns.NAME,
                RecipeContract.Ingredients.Columns.IMAGE,
                RecipeContract.Ingredients.Columns.AISLE
            )
        )

        runBlocking {
            val ingredients = ingredientDao.getAllIngredients().first()
            ingredients.forEach { ingredient ->
                cursor.addRow(arrayOf(
                    ingredient.id,
                    ingredient.name,
                    ingredient.image,
                    ingredient.aisle
                ))
            }
        }

        return cursor
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            RecipeContract.UriCodes.RECIPES -> RecipeContract.Recipes.CONTENT_TYPE
            RecipeContract.UriCodes.RECIPE_ID -> RecipeContract.Recipes.CONTENT_ITEM_TYPE
            RecipeContract.UriCodes.SAVED_RECIPES -> RecipeContract.SavedRecipes.CONTENT_TYPE
            RecipeContract.UriCodes.SAVED_RECIPE_ID -> RecipeContract.SavedRecipes.CONTENT_ITEM_TYPE
            RecipeContract.UriCodes.INGREDIENTS -> RecipeContract.Ingredients.CONTENT_TYPE
            else -> null
        }
    }

    // Read-only provider - insert, update, delete return null/0
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
