package com.foodsnap.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodsnap.domain.model.Recipe
import com.foodsnap.domain.usecase.recipe.GetRandomRecipesUseCase
import com.foodsnap.domain.usecase.recipe.SearchRecipesUseCase
import com.foodsnap.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the Home screen.
 *
 * @property recipes List of recipes to display
 * @property searchQuery Current search query
 * @property selectedCategory Currently selected category filter ID
 * @property selectedCategoryApiValue API value for the selected category
 * @property isLoading Whether data is being loaded
 * @property isRefreshing Whether pull-to-refresh is active
 * @property error Error message if any
 */
data class HomeUiState(
    val recipes: List<Recipe> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "all",
    val selectedCategoryApiValue: String? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val randomRecipeId: Long? = null
)

/**
 * ViewModel for the Home screen.
 *
 * Manages the UI state and handles user interactions for recipe discovery.
 * Loads featured/random recipes on initialization and handles search.
 *
 * @property searchRecipesUseCase Use case for searching recipes
 * @property getRandomRecipesUseCase Use case for getting random/featured recipes
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val searchRecipesUseCase: SearchRecipesUseCase,
    private val getRandomRecipesUseCase: GetRandomRecipesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadFeaturedRecipes()
    }

    /**
     * Loads featured/random recipes for the home screen.
     */
    private fun loadFeaturedRecipes(isRefreshing: Boolean = false) {
        viewModelScope.launch {
            val tags = _uiState.value.selectedCategoryApiValue
            getRandomRecipesUseCase(count = 10, tags = tags).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update {
                            it.copy(
                                isLoading = !isRefreshing,
                                isRefreshing = isRefreshing,
                                error = null
                            )
                        }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                recipes = result.data ?: emptyList(),
                                isLoading = false,
                                isRefreshing = false,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = result.message,
                                recipes = result.data ?: it.recipes
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Updates the search query.
     *
     * @param query The new search query
     */
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * Updates the selected category filter.
     *
     * @param categoryId The selected category ID
     * @param apiValue The API value for the category
     */
    fun onCategorySelected(categoryId: String, apiValue: String?) {
        _uiState.update {
            it.copy(
                selectedCategory = categoryId,
                selectedCategoryApiValue = apiValue
            )
        }

        if (apiValue == null) {
            loadFeaturedRecipes()
        } else {
            searchByCategory(apiValue)
        }
    }

    /**
     * Searches recipes by category/type.
     */
    private fun searchByCategory(categoryType: String) {
        viewModelScope.launch {
            searchRecipesUseCase(query = "", type = categoryType).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                recipes = result.data ?: emptyList(),
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message,
                                recipes = result.data ?: it.recipes
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Refreshes the recipe list.
     */
    fun refresh() {
        if (_uiState.value.selectedCategoryApiValue == null) {
            loadFeaturedRecipes(isRefreshing = true)
        } else {
            _uiState.update { it.copy(isRefreshing = true) }
            searchByCategory(_uiState.value.selectedCategoryApiValue!!)
        }
    }

    /**
     * Gets a random recipe for the shake-to-discover feature.
     * Sets randomRecipeId in UI state which triggers navigation.
     */
    fun getRandomRecipe() {
        viewModelScope.launch {
            getRandomRecipesUseCase(count = 1, tags = null).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.firstOrNull()?.let { recipe ->
                            _uiState.update { it.copy(randomRecipeId = recipe.id) }
                        }
                    }
                    else -> { /* Ignore loading/error for shake */ }
                }
            }
        }
    }

    /**
     * Clears the random recipe ID after navigation.
     */
    fun clearRandomRecipe() {
        _uiState.update { it.copy(randomRecipeId = null) }
    }
}
