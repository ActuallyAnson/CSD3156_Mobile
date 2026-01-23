package com.foodsnap.presentation.navigation

/**
 * Sealed class representing all navigation destinations in the app.
 *
 * Each screen has a unique route string used by Navigation Compose.
 * Screens with arguments define helper functions to create the route with parameters.
 */
sealed class Screen(val route: String) {

    /**
     * Splash screen - animated branding shown on app launch.
     */
    data object Splash : Screen("splash")

    /**
     * Home screen - main entry point showing featured recipes and categories.
     */
    data object Home : Screen("home")

    /**
     * Camera screen - for scanning barcodes, ingredients, or dishes.
     * @param mode Optional camera mode (barcode, ingredient, dish)
     */
    data object Camera : Screen("camera?mode={mode}") {
        fun createRoute(mode: CameraMode = CameraMode.BARCODE): String {
            return "camera?mode=${mode.name}"
        }
    }

    /**
     * Recipe detail screen - shows full recipe information.
     * @param recipeId The ID of the recipe to display
     */
    data object RecipeDetail : Screen("recipe/{recipeId}") {
        fun createRoute(recipeId: Long): String {
            return "recipe/$recipeId"
        }
    }

    /**
     * Saved recipes screen - shows user's favorite recipes.
     */
    data object SavedRecipes : Screen("saved")

    /**
     * User inventory screen - shows user's pantry/ingredients.
     */
    data object Inventory : Screen("inventory")

    /**
     * Search results screen - shows recipes matching a query.
     * @param query The search query or ingredient list
     */
    data object Search : Screen("search?query={query}") {
        fun createRoute(query: String): String {
            return "search?query=$query"
        }
    }

    /**
     * Cooking mode screen - full-screen step-by-step instructions.
     * @param recipeId The ID of the recipe to cook
     */
    data object CookingMode : Screen("cooking/{recipeId}") {
        fun createRoute(recipeId: Long): String {
            return "cooking/$recipeId"
        }
    }

    companion object {
        /**
         * List of screens that should show the bottom navigation bar.
         */
        val bottomNavScreens = listOf(Home, Camera, SavedRecipes, Inventory)
    }
}

/**
 * Enum representing the different camera modes available.
 */
enum class CameraMode {
    /**
     * Scan product barcodes to identify ingredients.
     */
    BARCODE,

    /**
     * Recognize fresh ingredients using image labeling.
     */
    INGREDIENT,

    /**
     * Recognize prepared dishes to find similar recipes.
     */
    DISH
}
