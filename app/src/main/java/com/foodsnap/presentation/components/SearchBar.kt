package com.foodsnap.presentation.components

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.foodsnap.R
import com.foodsnap.presentation.theme.CustomShapes
import com.foodsnap.util.SpeechRecognitionHelper

/**
 * Search bar component for recipe search with voice input.
 *
 * Features:
 * - Search icon on the left
 * - Clear button when text is present
 * - Voice search microphone button
 * - IME search action
 * - Rounded shape following Material Design 3
 *
 * @param query Current search query
 * @param onQueryChange Callback when query changes
 * @param onSearch Callback when search is submitted
 * @param modifier Optional modifier for styling
 * @param placeholder Placeholder text
 * @param showVoiceSearch Whether to show voice search button
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.search_recipes),
    showVoiceSearch: Boolean = true
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Speech recognition
    val speechHelper = remember { SpeechRecognitionHelper(context) }
    val isListening by speechHelper.isListening.collectAsState()
    val recognizedText by speechHelper.recognizedText.collectAsState()
    val speechError by speechHelper.error.collectAsState()

    // Permission launcher for microphone
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            speechHelper.startListening()
        } else {
            Toast.makeText(context, "Microphone permission required for voice search", Toast.LENGTH_SHORT).show()
        }
    }

    // Update query when speech is recognized
    LaunchedEffect(recognizedText) {
        recognizedText?.let { text ->
            onQueryChange(text)
            speechHelper.clearRecognizedText()
        }
    }

    // Show error toast
    LaunchedEffect(speechError) {
        speechError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            speechHelper.clearError()
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            speechHelper.destroy()
        }
    }

    // Animated mic button color
    val micColor by animateColorAsState(
        targetValue = if (isListening) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.primary
        },
        label = "mic_color"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(text = if (isListening) "Listening..." else placeholder)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            singleLine = true,
            shape = CustomShapes.SearchBar,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearch()
                    focusManager.clearFocus()
                }
            )
        )

        // Voice search button
        if (showVoiceSearch && speechHelper.isAvailable()) {
            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (isListening) {
                        speechHelper.stopListening()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = micColor
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = if (isListening) "Stop listening" else "Voice search",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
