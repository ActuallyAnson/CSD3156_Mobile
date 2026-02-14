package com.foodsnap.presentation.screen.home

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foodsnap.R
import com.foodsnap.presentation.components.CategoryChips
import com.foodsnap.presentation.components.RecipeCard
import com.foodsnap.presentation.components.SearchBar
import com.foodsnap.presentation.components.ShimmerRecipeGrid
import com.foodsnap.presentation.components.defaultCategories
import com.foodsnap.presentation.navigation.CameraMode
import com.foodsnap.util.ShakeDetector

/**
 * Home screen displaying featured recipes, categories, and search.
 *
 * This is the main entry point of the app showing:
 * - Search bar for recipe discovery
 * - Category filter chips
 * - Grid of recipe cards with refresh button
 *
 * @param onRecipeClick Callback when a recipe card is clicked
 * @param onCameraClick Callback when camera mode is selected
 * @param onSearchSubmit Callback when search is submitted
 * @param viewModel The ViewModel for this screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onRecipeClick: (Long) -> Unit,
    onCameraClick: (CameraMode) -> Unit,
    onSearchSubmit: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Shake detection for random recipe
    val shakeDetector = remember {
        ShakeDetector(context) {
            Toast.makeText(context, "Finding a random recipe...", Toast.LENGTH_SHORT).show()
            viewModel.getRandomRecipe()
        }
    }

    // Start/stop shake detector with lifecycle
    DisposableEffect(Unit) {
        shakeDetector.start()
        onDispose {
            shakeDetector.stop()
        }
    }

    // Navigate to random recipe when found
    LaunchedEffect(uiState.randomRecipeId) {
        uiState.randomRecipeId?.let { recipeId ->
            onRecipeClick(recipeId)
            viewModel.clearRandomRecipe()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !uiState.isRefreshing
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = androidx.compose.ui.Modifier
                                    .padding(8.dp)
                                    .height(24.dp)
                                    .fillMaxWidth(0.5f),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh"
                            )
                        }
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onSearch = { onSearchSubmit(uiState.searchQuery) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Chips
            CategoryChips(
                categories = defaultCategories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { category ->
                    viewModel.onCategorySelected(category.id, category.apiValue)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Recipe Grid Content
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading && uiState.recipes.isEmpty() -> {
                        ShimmerRecipeGrid(
                            modifier = Modifier.fillMaxSize(),
                            itemCount = 6
                        )
                    }
                    uiState.error != null && uiState.recipes.isEmpty() -> {
                        ErrorContent(
                            message = uiState.error ?: stringResource(R.string.error_occurred),
                            onRetry = { viewModel.refresh() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    uiState.recipes.isEmpty() -> {
                        EmptyContent(
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = uiState.recipes,
                                key = { it.id }
                            ) { recipe ->
                                RecipeCard(
                                    recipe = recipe,
                                    onClick = { onRecipeClick(recipe.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        androidx.compose.material3.TextButton(onClick = onRetry) {
            Text(text = stringResource(R.string.retry))
        }
    }
}

@Composable
private fun EmptyContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No recipes found",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Try a different category or search term",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
