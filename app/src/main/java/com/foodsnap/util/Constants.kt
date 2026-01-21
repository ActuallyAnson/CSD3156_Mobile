package com.foodsnap.util

/**
 * Application-wide constants.
 *
 * This object contains all constant values used throughout the app,
 * including API endpoints, database configuration, and feature flags.
 */
object Constants {

    // API Configuration
    const val SPOONACULAR_BASE_URL = "https://api.spoonacular.com/"
    const val OPEN_FOOD_FACTS_BASE_URL = "https://world.openfoodfacts.org/"

    // Database
    const val DATABASE_NAME = "foodsnap_database"
    const val DATABASE_VERSION = 1

    // Network
    const val NETWORK_TIMEOUT_SECONDS = 30L
    const val CACHE_SIZE_MB = 10L
    const val CACHE_MAX_AGE_HOURS = 24

    // Pagination
    const val DEFAULT_PAGE_SIZE = 20
    const val INITIAL_LOAD_SIZE = 40

    // Content Provider
    const val CONTENT_AUTHORITY = "com.foodsnap.provider"
    const val CONTENT_PATH_RECIPES = "recipes"
    const val CONTENT_PATH_INGREDIENTS = "ingredients"

    // ML Kit
    const val BARCODE_CONFIDENCE_THRESHOLD = 0.5f
    const val IMAGE_LABEL_CONFIDENCE_THRESHOLD = 0.7f
    const val DISH_RECOGNITION_CONFIDENCE_THRESHOLD = 0.6f

    // AR
    const val AR_PLANE_FINDING_MODE_HORIZONTAL = true
    const val AR_MODEL_SCALE_DEFAULT = 0.5f
    const val AR_MODEL_SCALE_MIN = 0.1f
    const val AR_MODEL_SCALE_MAX = 2.0f

    // WorkManager
    const val WORK_CACHE_CLEANUP = "cache_cleanup_work"
    const val WORK_EXPIRY_CHECK = "expiry_check_work"
    const val WORK_SYNC_RECIPES = "sync_recipes_work"

    // Preferences Keys
    const val PREF_DARK_MODE = "dark_mode"
    const val PREF_DIETARY_PREFERENCES = "dietary_preferences"
    const val PREF_LAST_SYNC = "last_sync"

    // Deep Link
    const val DEEP_LINK_SCHEME = "https"
    const val DEEP_LINK_HOST = "foodsnap.app"
    const val DEEP_LINK_PATH_RECIPE = "/recipe"

    // Categories
    val RECIPE_CATEGORIES = listOf(
        "All",
        "Breakfast",
        "Lunch",
        "Dinner",
        "Dessert",
        "Snack",
        "Vegetarian",
        "Vegan"
    )

    // Dietary Options
    val DIETARY_OPTIONS = listOf(
        "Vegetarian",
        "Vegan",
        "Gluten Free",
        "Dairy Free",
        "Ketogenic",
        "Paleo"
    )

    // Cuisine Types
    val CUISINE_TYPES = listOf(
        "American",
        "Asian",
        "British",
        "Chinese",
        "French",
        "Indian",
        "Italian",
        "Japanese",
        "Korean",
        "Mediterranean",
        "Mexican",
        "Thai"
    )
}
