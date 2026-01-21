package com.foodsnap.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.foodsnap.R
import com.foodsnap.presentation.navigation.Screen

/**
 * Data class representing a bottom navigation item.
 *
 * @property route The navigation route for this item
 * @property titleResId String resource ID for the item's title
 * @property selectedIcon Icon when this item is selected
 * @property unselectedIcon Icon when this item is not selected
 */
data class BottomNavItem(
    val route: String,
    val titleResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

/**
 * List of bottom navigation items.
 */
val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Home.route,
        titleResId = R.string.nav_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        route = Screen.Camera.route,
        titleResId = R.string.nav_camera,
        selectedIcon = Icons.Filled.CameraAlt,
        unselectedIcon = Icons.Outlined.CameraAlt
    ),
    BottomNavItem(
        route = Screen.SavedRecipes.route,
        titleResId = R.string.nav_saved,
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder
    ),
    BottomNavItem(
        route = Screen.Inventory.route,
        titleResId = R.string.nav_inventory,
        selectedIcon = Icons.Filled.Kitchen,
        unselectedIcon = Icons.Outlined.Kitchen
    )
)

/**
 * Bottom navigation bar for the FoodSnap app.
 *
 * Shows navigation items for Home, Camera, Saved Recipes, and Inventory.
 * Highlights the current destination and handles navigation.
 *
 * @param navController The NavController for navigation
 * @param modifier Optional modifier for styling
 */
@Composable
fun BottomNavBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(modifier = modifier) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute?.startsWith(item.route.substringBefore("?")) == true

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = stringResource(id = item.titleResId)
                    )
                },
                label = { Text(text = stringResource(id = item.titleResId)) },
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination to avoid building up a large stack
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

/**
 * Determines whether the bottom navigation bar should be visible for the current route.
 *
 * @param currentRoute The current navigation route
 * @return True if the bottom nav should be visible
 */
fun shouldShowBottomNav(currentRoute: String?): Boolean {
    if (currentRoute == null) return true

    val baseRoute = currentRoute.substringBefore("?").substringBefore("/")
    return bottomNavItems.any { item ->
        item.route.substringBefore("?").substringBefore("/") == baseRoute
    }
}
