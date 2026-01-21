package com.foodsnap.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a recipe in the local database.
 *
 * Stores recipe data fetched from the API and cached locally.
 * Supports offline-first functionality.
 *
 * @property id Local database ID
 * @property spoonacularId Original ID from Spoonacular API
 * @property title Recipe name
 * @property summary Brief description (may contain HTML)
 * @property instructions Cooking instructions as JSON string
 * @property imageUrl URL of the recipe image
 * @property servings Number of servings
 * @property readyInMinutes Total cooking time
 * @property sourceUrl Original recipe URL
 * @property cuisines List of cuisine types as JSON
 * @property dishTypes List of dish types as JSON
 * @property diets List of diet types as JSON
 * @property calories Calorie count per serving
 * @property protein Protein in grams
 * @property fat Fat in grams
 * @property carbs Carbohydrates in grams
 * @property createdAt Timestamp when cached
 * @property isCached Whether this is a cached entry
 * @property arModelPath Path to AR model asset
 */
@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey
    val id: Long,
    val spoonacularId: Int,
    val title: String,
    val summary: String = "",
    val instructions: String? = null,
    val imageUrl: String? = null,
    val servings: Int = 1,
    val readyInMinutes: Int = 0,
    val sourceUrl: String? = null,
    val cuisines: String = "[]",
    val dishTypes: String = "[]",
    val diets: String = "[]",
    val calories: Double? = null,
    val protein: Double? = null,
    val fat: Double? = null,
    val carbs: Double? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isCached: Boolean = true,
    val arModelPath: String? = null,
    val vegetarian: Boolean = false,
    val vegan: Boolean = false,
    val glutenFree: Boolean = false,
    val dairyFree: Boolean = false,
    val healthScore: Double? = null,
    val spoonacularScore: Double? = null,
    val pricePerServing: Double? = null
)
