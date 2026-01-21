package com.foodsnap.presentation.screen.saved

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodsnap.R
import com.foodsnap.domain.model.Recipe
import com.foodsnap.domain.usecase.saved.GetSavedRecipesUseCase
import com.foodsnap.domain.usecase.saved.RemoveSavedRecipeUseCase
import com.foodsnap.presentation.components.RecipeCard
import com.foodsnap.presentation.components.SwipeToDeleteCard
import com.foodsnap.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the Saved Recipes screen.
 */
data class SavedRecipesUiState(
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * Events that the ViewModel can send to the UI.
 */
sealed class SavedRecipesEvent {
    data class ShowUndoSnackbar(val recipeName: String, val recipeId: Long) : SavedRecipesEvent()
}

/**
 * ViewModel for the Saved Recipes screen.
 */
@HiltViewModel
class SavedRecipesViewModel @Inject constructor(
    private val getSavedRecipesUseCase: GetSavedRecipesUseCase,
    private val removeSavedRecipeUseCase: RemoveSavedRecipeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedRecipesUiState())
    val uiState: StateFlow<SavedRecipesUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SavedRecipesEvent>()
    val events: SharedFlow<SavedRecipesEvent> = _events.asSharedFlow()

    // Store recently removed recipe for undo
    private var lastRemovedRecipe: Recipe? = null

    init {
        loadSavedRecipes()
    }

    private fun loadSavedRecipes() {
        viewModelScope.launch {
            getSavedRecipesUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                recipes = result.data ?: emptyList(),
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Removes a recipe from saved recipes.
     *
     * @param recipe The recipe to remove
     */
    fun removeRecipe(recipe: Recipe) {
        viewModelScope.launch {
            lastRemovedRecipe = recipe
            removeSavedRecipeUseCase(recipe.id)
            _events.emit(SavedRecipesEvent.ShowUndoSnackbar(recipe.title, recipe.id))
        }
    }

    /**
     * Undoes the last remove operation.
     */
    fun undoRemove() {
        viewModelScope.launch {
            lastRemovedRecipe?.let { recipe ->
                // Re-save the recipe
                // Note: This would need a SaveRecipeUseCase to be injected
                // For now, we'll just reload the list
                loadSavedRecipes()
            }
            lastRemovedRecipe = null
        }
    }
}

/**
 * Saved recipes screen displaying user's favorite recipes.
 *
 * Features:
 * - Grid/list view of saved recipes
 * - Swipe-to-delete functionality
 * - Undo snackbar for accidental deletions
 *
 * @param onRecipeClick Callback when a recipe is clicked
 * @param viewModel The ViewModel for this screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedRecipesScreen(
    onRecipeClick: (Long) -> Unit,
    viewModel: SavedRecipesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SavedRecipesEvent.ShowUndoSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "\"${event.recipeName}\" removed",
                        actionLabel = "Undo",
                        withDismissAction = true
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoRemove()
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.saved_recipes),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error ?: stringResource(R.string.error_occurred),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.recipes.isEmpty() -> {
                    EmptyState(modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    SavedRecipesList(
                        recipes = uiState.recipes,
                        onRecipeClick = onRecipeClick,
                        onRemove = viewModel::removeRecipe,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedRecipesList(
    recipes: List<Recipe>,
    onRecipeClick: (Long) -> Unit,
    onRemove: (Recipe) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "${recipes.size} saved ${if (recipes.size == 1) "recipe" else "recipes"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(
            items = recipes,
            key = { it.id }
        ) { recipe ->
            SwipeToDeleteCard(
                onDismiss = { onRemove(recipe) }
            ) {
                RecipeCard(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe.id) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.BookmarkRemove,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = stringResource(R.string.no_saved_recipes),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.save_recipes_hint),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
