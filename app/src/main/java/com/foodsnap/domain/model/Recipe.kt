package com.foodsnap.domain.model

/**
 * Domain model representing a recipe.
 *
 * This is the core data class used throughout the app for recipe information.
 * It's independent of data sources (API DTOs, database entities).
 *
 * @property id Unique identifier
 * @property title Recipe name
 * @property summary Brief description
 * @property imageUrl URL of the recipe image
 * @property readyInMinutes Total cooking time in minutes
 * @property servings Number of servings
 * @property instructions List of cooking steps
 * @property ingredients List of ingredients with amounts
 * @property cuisines List of cuisine types (Italian, Mexican, etc.)
 * @property dishTypes List of dish types (main course, dessert, etc.)
 * @property diets List of applicable diets (vegetarian, vegan, etc.)
 * @property rating Average user rating (1-5)
 * @property calories Calorie count per serving
 * @property sourceUrl Original recipe URL
 * @property arModelUrl URL or asset path for 3D AR model
 */
data class Recipe(
    val id: Long,
    val title: String,
    val summary: String = "",
    val imageUrl: String? = null,
    val readyInMinutes: Int = 0,
    val servings: Int = 1,
    val instructions: List<String> = emptyList(),
    val ingredients: List<RecipeIngredient> = emptyList(),
    val cuisines: List<String> = emptyList(),
    val dishTypes: List<String> = emptyList(),
    val diets: List<String> = emptyList(),
    val rating: Float? = null,
    val calories: Int? = null,
    val sourceUrl: String? = null,
    val arModelUrl: String? = null
)

/**
 * Ingredient within a recipe, including amount and unit.
 *
 * @property id Ingredient identifier
 * @property name Ingredient name
 * @property amount Quantity needed
 * @property unit Unit of measurement
 * @property original Original text (e.g., "2 cups flour")
 */
data class RecipeIngredient(
    val id: Long,
    val name: String,
    val amount: Double,
    val unit: String,
    val original: String = ""
)
