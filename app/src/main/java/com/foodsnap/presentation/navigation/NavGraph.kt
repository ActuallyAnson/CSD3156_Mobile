package com.foodsnap.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.foodsnap.presentation.screen.camera.CameraScreen
import com.foodsnap.presentation.screen.cooking.CookingModeScreen
import com.foodsnap.presentation.screen.detail.RecipeDetailScreen
import com.foodsnap.presentation.screen.home.HomeScreen
import com.foodsnap.presentation.screen.inventory.UserInventoryScreen
import com.foodsnap.presentation.screen.saved.SavedRecipesScreen
import com.foodsnap.presentation.screen.search.SearchResultsScreen
import com.foodsnap.presentation.screen.splash.SplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.foodsnap.presentation.screen.detail.RecipeDetailViewModel

/**
 * Main navigation graph for the FoodSnap app.
 *
 * Defines all navigation routes and their composable destinations.
 * Handles argument passing between screens.
 *
 * @param navController The NavHostController managing navigation
 * @param modifier Optional modifier for the NavHost
 */
@Composable
fun FoodSnapNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Home Screen
        composable(Screen.Home.route) {
            HomeScreen(
                onRecipeClick = { recipeId ->
                    navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                },
                onCameraClick = { mode ->
                    navController.navigate(Screen.Camera.createRoute(mode))
                },
                onSearchSubmit = { query ->
                    navController.navigate(Screen.Search.createRoute(query))
                }
            )
        }

        // Camera Screen
        composable(
            route = Screen.Camera.route,
            arguments = listOf(
                navArgument("mode") {
                    type = NavType.StringType
                    defaultValue = CameraMode.BARCODE.name
                }
            )
        ) { backStackEntry ->
            val modeString = backStackEntry.arguments?.getString("mode") ?: CameraMode.BARCODE.name
            val mode = try {
                CameraMode.valueOf(modeString)
            } catch (e: IllegalArgumentException) {
                CameraMode.BARCODE
            }

            CameraScreen(
                initialMode = mode,
                onScanResult = { result ->
                    // Navigate based on scan result type
                    when {
                        result.recipeId != null -> {
                            navController.navigate(Screen.RecipeDetail.createRoute(result.recipeId))
                        }
                        result.ingredients.isNotEmpty() -> {
                            navController.navigate(
                                Screen.Search.createRoute(result.ingredients.joinToString(","))
                            )
                        }
                        result.productName != null -> {
                            navController.navigate(Screen.Search.createRoute(result.productName))
                        }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Recipe Detail Screen
        composable(
            route = Screen.RecipeDetail.route,
            arguments = listOf(
                navArgument("recipeId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: return@composable

            RecipeDetailScreen(
                recipeId = recipeId,
                onBackClick = { navController.popBackStack() },
                onStartCooking = {
                    navController.navigate(Screen.CookingMode.createRoute(recipeId))
                }
            )
        }

        // Cooking Mode Screen
        composable(
            route = Screen.CookingMode.route,
            arguments = listOf(
                navArgument("recipeId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: return@composable
            val viewModel: RecipeDetailViewModel = hiltViewModel()

            val uiState = viewModel.uiState.collectAsState().value
            val recipe = uiState.recipe

            if (recipe != null) {
                CookingModeScreen(
                    recipeTitle = recipe.title,
                    instructions = recipe.instructions,
                    onClose = { navController.popBackStack() }
                )
            }
        }

        // Saved Recipes Screen
        composable(Screen.SavedRecipes.route) {
            SavedRecipesScreen(
                onRecipeClick = { recipeId ->
                    navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                }
            )
        }

        // User Inventory Screen
        composable(Screen.Inventory.route) {
            UserInventoryScreen(
                onScanClick = {
                    navController.navigate(Screen.Camera.createRoute(CameraMode.INGREDIENT))
                },
                onFindRecipesClick = { ingredients ->
                    navController.navigate(
                        Screen.Search.createRoute(ingredients.joinToString(","))
                    )
                }
            )
        }

        // Search Results Screen
        composable(
            route = Screen.Search.route,
            arguments = listOf(
                navArgument("query") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query") ?: ""

            SearchResultsScreen(
                initialQuery = query,
                onRecipeClick = { recipeId ->
                    navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
