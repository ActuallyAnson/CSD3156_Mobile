package com.foodsnap.presentation.screen.ar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.foodsnap.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * UI state for the AR Preview screen.
 */
data class ARPreviewUiState(
    val recipeId: Long = 0L,
    val modelUrl: String? = null,
    val isARSupported: Boolean = true,
    val isModelLoaded: Boolean = false,
    val isPlaced: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the AR Preview screen.
 */
@HiltViewModel
class ARPreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val recipeId: Long = savedStateHandle.get<Long>("recipeId") ?: 0L

    private val _uiState = MutableStateFlow(ARPreviewUiState(recipeId = recipeId))
    val uiState: StateFlow<ARPreviewUiState> = _uiState.asStateFlow()

    init {
        loadModel()
    }

    private fun loadModel() {
        // TODO: Load 3D model URL based on recipe category
        // For now, use a placeholder model
        _uiState.update {
            it.copy(
                modelUrl = "models/burger.glb",
                isModelLoaded = true
            )
        }
    }

    fun onModelPlaced() {
        _uiState.update { it.copy(isPlaced = true) }
    }

    fun reset() {
        _uiState.update { it.copy(isPlaced = false) }
    }

    fun setARNotSupported() {
        _uiState.update { it.copy(isARSupported = false) }
    }

    fun setError(message: String) {
        _uiState.update { it.copy(error = message) }
    }
}

/**
 * AR Preview screen for displaying 3D dish models.
 *
 * Uses ARCore for plane detection and model placement.
 * Supports scale and rotate gestures.
 *
 * @param recipeId ID of the recipe to preview
 * @param onBackClick Callback when back button is pressed
 * @param viewModel The ViewModel for this screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ARPreviewScreen(
    recipeId: Long,
    onBackClick: () -> Unit,
    viewModel: ARPreviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.ar_preview)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back_button)
                        )
                    }
                },
                actions = {
                    if (uiState.isPlaced) {
                        IconButton(onClick = viewModel::reset) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.ar_reset)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                !uiState.isARSupported -> {
                    ARNotSupportedContent(
                        onBackClick = onBackClick,
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
                !uiState.isModelLoaded -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = stringResource(R.string.ar_loading_model),
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
                else -> {
                    ARSceneContent(
                        modelUrl = uiState.modelUrl,
                        isPlaced = uiState.isPlaced,
                        onModelPlaced = viewModel::onModelPlaced,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Instruction overlay
                    if (!uiState.isPlaced) {
                        ARInstructionsOverlay(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ARNotSupportedContent(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ViewInAr,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.ar_not_supported),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your device does not support ARCore, which is required for the AR preview feature.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBackClick) {
            Text("Go Back")
        }
    }
}

@Composable
private fun ARSceneContent(
    modelUrl: String?,
    isPlaced: Boolean,
    onModelPlaced: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Placeholder for actual AR implementation in Phase 8
    // Will be replaced with SceneView ARCore integration
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ViewInAr,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "AR Preview",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = if (isPlaced) "Model placed on surface" else "Camera view will appear here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Gesture instructions
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                GestureHint(
                    icon = Icons.Default.TouchApp,
                    label = "Tap to place"
                )
                GestureHint(
                    icon = Icons.Default.RotateRight,
                    label = "Pinch to scale"
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Simulate placement button for testing
            if (!isPlaced) {
                Button(onClick = onModelPlaced) {
                    Text(text = "Simulate Tap to Place")
                }
            }
        }
    }
}

@Composable
private fun GestureHint(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ARInstructionsOverlay(
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = stringResource(R.string.ar_instruction),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
