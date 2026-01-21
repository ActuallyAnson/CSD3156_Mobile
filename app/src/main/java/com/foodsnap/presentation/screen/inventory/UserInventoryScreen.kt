package com.foodsnap.presentation.screen.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodsnap.R
import com.foodsnap.domain.model.UserIngredient
import com.foodsnap.domain.usecase.inventory.GetUserInventoryUseCase
import com.foodsnap.presentation.theme.ExpiredColor
import com.foodsnap.presentation.theme.ExpiringSoonColor
import com.foodsnap.util.Resource
import com.foodsnap.util.isWithinDays
import com.foodsnap.util.isPast
import com.foodsnap.util.toFormattedDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the User Inventory screen.
 */
data class UserInventoryUiState(
    val ingredients: List<UserIngredient> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel for the User Inventory screen.
 */
@HiltViewModel
class UserInventoryViewModel @Inject constructor(
    private val getUserInventoryUseCase: GetUserInventoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserInventoryUiState())
    val uiState: StateFlow<UserInventoryUiState> = _uiState.asStateFlow()

    init {
        loadInventory()
    }

    private fun loadInventory() {
        viewModelScope.launch {
            getUserInventoryUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                ingredients = result.data ?: emptyList(),
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
}

/**
 * User inventory screen for managing pantry ingredients.
 *
 * @param onScanClick Callback when scan button is clicked
 * @param onFindRecipesClick Callback when find recipes button is clicked
 * @param viewModel The ViewModel for this screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInventoryScreen(
    onScanClick: () -> Unit,
    onFindRecipesClick: (List<String>) -> Unit,
    viewModel: UserInventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.my_inventory)) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onScanClick) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = stringResource(R.string.add_ingredient)
                )
            }
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
                uiState.ingredients.isEmpty() -> {
                    EmptyInventory(
                        onScanClick = onScanClick,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    InventoryContent(
                        ingredients = uiState.ingredients,
                        onFindRecipesClick = {
                            onFindRecipesClick(uiState.ingredients.map { it.name })
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyInventory(
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.no_ingredients),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.scan_to_add),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onScanClick) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text = stringResource(R.string.add_ingredient))
        }
    }
}

@Composable
private fun InventoryContent(
    ingredients: List<UserIngredient>,
    onFindRecipesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Find Recipes Button
        OutlinedButton(
            onClick = onFindRecipesClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text = stringResource(R.string.find_recipes))
        }

        // Ingredients List
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = ingredients,
                key = { it.id }
            ) { ingredient ->
                IngredientCard(ingredient = ingredient)
            }
        }
    }
}

@Composable
private fun IngredientCard(
    ingredient: UserIngredient,
    modifier: Modifier = Modifier
) {
    val expiryStatus = when {
        ingredient.expirationDate?.isPast() == true -> ExpiryStatus.EXPIRED
        ingredient.expirationDate?.isWithinDays(3) == true -> ExpiryStatus.EXPIRING_SOON
        else -> ExpiryStatus.FRESH
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${ingredient.quantity} ${ingredient.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (ingredient.expirationDate != null) {
                Text(
                    text = when (expiryStatus) {
                        ExpiryStatus.EXPIRED -> stringResource(R.string.expired)
                        ExpiryStatus.EXPIRING_SOON -> stringResource(R.string.expiring_soon)
                        ExpiryStatus.FRESH -> ingredient.expirationDate.toFormattedDate()
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = when (expiryStatus) {
                        ExpiryStatus.EXPIRED -> ExpiredColor
                        ExpiryStatus.EXPIRING_SOON -> ExpiringSoonColor
                        ExpiryStatus.FRESH -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

private enum class ExpiryStatus {
    FRESH, EXPIRING_SOON, EXPIRED
}
