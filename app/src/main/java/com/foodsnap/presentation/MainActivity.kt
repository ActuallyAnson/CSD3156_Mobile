package com.foodsnap.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.foodsnap.presentation.components.BottomNavBar
import com.foodsnap.presentation.components.shouldShowBottomNav
import com.foodsnap.presentation.navigation.FoodSnapNavGraph
import com.foodsnap.presentation.theme.FoodSnapTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the FoodSnap app.
 *
 * This is the single activity that hosts all Compose screens.
 * It sets up:
 * - Edge-to-edge display
 * - The FoodSnap theme
 * - Navigation with bottom bar
 * - Hilt dependency injection
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        setContent {
            FoodSnapTheme {
                FoodSnapApp()
            }
        }
    }
}

/**
 * Main app composable containing the scaffold and navigation.
 *
 * Sets up the app structure with:
 * - Scaffold with bottom navigation
 * - Animated bottom bar visibility
 * - Navigation host for all screens
 */
@Composable
fun FoodSnapApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Animate bottom bar visibility
            AnimatedVisibility(
                visible = shouldShowBottomNav(currentRoute),
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        FoodSnapNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
