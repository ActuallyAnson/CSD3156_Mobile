package com.foodsnap.presentation.screen.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodsnap.data.remote.api.OpenFoodFactsApi
import com.foodsnap.ml.AnalyzerResult
import com.foodsnap.ml.BarcodeAnalyzer
import com.foodsnap.ml.DishRecognitionAnalyzer
import com.foodsnap.ml.ImageLabelAnalyzer
import com.foodsnap.presentation.navigation.CameraMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the Camera screen.
 *
 * @property currentMode Current scanning mode
 * @property isScanning Whether scanning is in progress
 * @property scanResult Latest scan result
 * @property detectedItems Currently detected items (for live preview)
 * @property error Error message if any
 */
data class CameraUiState(
    val currentMode: CameraMode = CameraMode.BARCODE,
    val isScanning: Boolean = true,
    val scanResult: ScanResult? = null,
    val detectedItems: List<String> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel for the Camera screen.
 *
 * Manages camera mode state and scan results.
 * Integrates with ML Kit for barcode scanning, image labeling, and dish recognition.
 *
 * @property barcodeAnalyzer Analyzer for barcode detection
 * @property imageLabelAnalyzer Analyzer for ingredient recognition
 * @property dishRecognitionAnalyzer Analyzer for dish recognition
 * @property openFoodFactsApi API for product lookup by barcode
 */
@HiltViewModel
class CameraViewModel @Inject constructor(
    val barcodeAnalyzer: BarcodeAnalyzer,
    val imageLabelAnalyzer: ImageLabelAnalyzer,
    val dishRecognitionAnalyzer: DishRecognitionAnalyzer,
    private val openFoodFactsApi: OpenFoodFactsApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    init {
        setupAnalyzers()
    }

    /**
     * Sets up the ML analyzer callbacks.
     */
    private fun setupAnalyzers() {
        // Barcode analyzer callbacks
        barcodeAnalyzer.setOnBarcodeDetectedListener { result ->
            onBarcodeScanned(result.barcode)
        }
        barcodeAnalyzer.setOnErrorListener { error ->
            setError(error.message)
        }

        // Image label analyzer callbacks
        imageLabelAnalyzer.setOnLabelsDetectedListener { result ->
            onIngredientsDetected(result.labels.map { it.text to it.confidence })
        }
        imageLabelAnalyzer.setOnErrorListener { error ->
            setError(error.message)
        }

        // Dish recognition analyzer callbacks
        dishRecognitionAnalyzer.setOnDishRecognizedListener { result ->
            onDishRecognized(result.dishName, result.confidence)
        }
        dishRecognitionAnalyzer.setOnErrorListener { error ->
            setError(error.message)
        }
    }

    /**
     * Sets the current camera mode.
     *
     * @param mode The new camera mode
     */
    fun setMode(mode: CameraMode) {
        _uiState.update {
            it.copy(
                currentMode = mode,
                scanResult = null,
                detectedItems = emptyList(),
                isScanning = true
            )
        }
    }

    /**
     * Gets the appropriate analyzer for the current mode.
     *
     * @return The ImageAnalysis.Analyzer for the current mode
     */
    fun getCurrentAnalyzer() = when (_uiState.value.currentMode) {
        CameraMode.BARCODE -> barcodeAnalyzer
        CameraMode.INGREDIENT -> imageLabelAnalyzer
        CameraMode.DISH -> dishRecognitionAnalyzer
    }

    /**
     * Processes a barcode scan result.
     *
     * @param barcode The scanned barcode value
     */
    private fun onBarcodeScanned(barcode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = false) }

            try {
                // Look up product by barcode using OpenFoodFacts API
                val response = openFoodFactsApi.getProductByBarcode(barcode)

                if (response.status == 1 && response.product != null) {
                    val product = response.product
                    val productName = product.productName ?: product.genericName ?: barcode

                    // Parse ingredients from ingredients text or use categories
                    val ingredients = product.ingredientsText?.split(",")
                        ?.map { it.trim() }
                        ?.filter { it.isNotEmpty() }
                        ?: product.categoriesTags?.map { it.removePrefix("en:") }
                        ?: emptyList()

                    _uiState.update {
                        it.copy(
                            scanResult = ScanResult(
                                productName = productName,
                                ingredients = ingredients,
                                confidence = 1f
                            )
                        )
                    }
                } else {
                    // Product not found, use barcode as search term
                    _uiState.update {
                        it.copy(
                            scanResult = ScanResult(productName = "Barcode: $barcode")
                        )
                    }
                }
            } catch (e: Exception) {
                // On error, just use the barcode value
                _uiState.update {
                    it.copy(
                        scanResult = ScanResult(productName = "Barcode: $barcode"),
                        error = "Could not look up product: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Processes image labeling results.
     *
     * @param labels List of detected labels with confidence
     */
    private fun onIngredientsDetected(labels: List<Pair<String, Float>>) {
        val ingredients = labels
            .filter { it.second >= 0.7f }
            .map { it.first }

        // Update detected items for live preview
        _uiState.update { it.copy(detectedItems = ingredients) }

        // Only create result if we have enough confidence and items
        if (ingredients.size >= 1 && labels.any { it.second >= 0.8f }) {
            _uiState.update {
                it.copy(
                    scanResult = ScanResult(
                        ingredients = ingredients,
                        confidence = labels.maxOf { label -> label.second }
                    ),
                    isScanning = false
                )
            }
        }
    }

    /**
     * Processes dish recognition result.
     *
     * @param dishName Name of the recognized dish
     * @param confidence Confidence score
     */
    private fun onDishRecognized(dishName: String, confidence: Float) {
        _uiState.update { it.copy(detectedItems = listOf(dishName)) }

        if (confidence >= 0.7f) {
            _uiState.update {
                it.copy(
                    scanResult = ScanResult(
                        ingredients = listOf(dishName),
                        confidence = confidence
                    ),
                    isScanning = false
                )
            }
        }
    }

    /**
     * Manually confirms the current detection.
     */
    fun confirmDetection() {
        val items = _uiState.value.detectedItems
        if (items.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    scanResult = ScanResult(
                        ingredients = items,
                        confidence = 1f
                    ),
                    isScanning = false
                )
            }
        }
    }

    /**
     * Clears the current scan result and resumes scanning.
     */
    fun clearScanResult() {
        _uiState.update {
            it.copy(
                scanResult = null,
                detectedItems = emptyList(),
                isScanning = true
            )
        }
    }

    /**
     * Sets an error message.
     *
     * @param message The error message
     */
    fun setError(message: String) {
        _uiState.update { it.copy(error = message) }
    }

    /**
     * Clears the error message.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up analyzers
        barcodeAnalyzer.close()
        imageLabelAnalyzer.close()
        dishRecognitionAnalyzer.close()
    }
}
