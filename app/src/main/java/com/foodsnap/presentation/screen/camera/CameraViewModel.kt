package com.foodsnap.presentation.screen.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodsnap.data.remote.api.OpenFoodFactsApi
import com.foodsnap.ml.AnalyzerResult
import com.foodsnap.ml.BoundingBox
import com.foodsnap.ml.BarcodeAnalyzer
import com.foodsnap.ml.DishRecognitionAnalyzer
import com.foodsnap.ml.ImageLabelAnalyzer
import com.foodsnap.presentation.navigation.CameraMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import androidx.compose.ui.geometry.Rect
import javax.inject.Inject
import java.util.concurrent.atomic.AtomicBoolean

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

    private val _overlayData = MutableStateFlow(AnalyzerOverlayData())
    private val barcodeInFlight = AtomicBoolean(false)
    private var lastHandledBarcode: String? = null
    private var lastHandledBarcodeAtMs: Long = 0L

    val overlayState: StateFlow<ScannerHudOverlayState> = combine(
        uiState,
        _overlayData
    ) { ui, overlay ->
        val statusText = when {
            ui.error != null -> "Error: ${ui.error}"
            ui.isScanning -> when (ui.currentMode) {
                CameraMode.BARCODE -> "Scanning barcode..."
                CameraMode.INGREDIENT -> "Searching..."
                CameraMode.DISH -> "Searching..."
            }
            else -> "Detected"
        }

        val labels = overlay.labels.take(3)
        val lowConfidence = labels.isNotEmpty() && labels.maxOf { it.confidence } < 0.70f

        ScannerHudOverlayState(
            mode = ui.currentMode,
            statusText = statusText,
            labels = labels,
            scanEnabled = ui.isScanning,
            boxes = overlay.boxes.take(10),
            qualityHints = ScannerHudQualityHints(lowConfidence = lowConfidence)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScannerHudOverlayState(
            mode = CameraMode.BARCODE,
            statusText = "Scanning barcode...",
            labels = emptyList(),
            scanEnabled = true,
            boxes = emptyList()
        )
    )

    init {
        setupAnalyzers()
    }

    /**
     * Sets up the ML analyzer callbacks.
     */
    private fun setupAnalyzers() {
        // Barcode analyzer callbacks
        barcodeAnalyzer.setOnBarcodeDetectedListener { result ->
            updateOverlayFromBarcode(result)
            val state = _uiState.value
            if (state.currentMode != CameraMode.BARCODE || !state.isScanning) return@setOnBarcodeDetectedListener

            val now = System.currentTimeMillis()
            if (result.barcode == lastHandledBarcode && now - lastHandledBarcodeAtMs < 10_000L) {
                return@setOnBarcodeDetectedListener
            }
            if (!barcodeInFlight.compareAndSet(false, true)) return@setOnBarcodeDetectedListener
            lastHandledBarcode = result.barcode
            lastHandledBarcodeAtMs = now
            onBarcodeScanned(result.barcode)
        }
        barcodeAnalyzer.setOnErrorListener { error ->
            setError(error.message)
        }

        // Image label analyzer callbacks
        imageLabelAnalyzer.setOnLabelsDetectedListener { result ->
            val state = _uiState.value
            if (state.currentMode != CameraMode.INGREDIENT || !state.isScanning) return@setOnLabelsDetectedListener
            updateOverlayLabels(result.labels.map { it.text to it.confidence })
            onIngredientsDetected(result.labels.map { it.text to it.confidence })
        }
        imageLabelAnalyzer.setOnErrorListener { error ->
            setError(error.message)
        }

        // Dish recognition analyzer callbacks
        dishRecognitionAnalyzer.setOnDishRecognizedListener { result ->
            val state = _uiState.value
            if (state.currentMode != CameraMode.DISH || !state.isScanning) return@setOnDishRecognizedListener
            updateOverlayFromDish(result.dishName, result.confidence, result.relatedLabels.map { it.text to it.confidence })
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
        _overlayData.value = AnalyzerOverlayData()
        barcodeInFlight.set(false)
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
        _uiState.update { it.copy(isScanning = false) }
        viewModelScope.launch {
            try {
                // Look up product by barcode using OpenFoodFacts API
                val response = openFoodFactsApi.getProductByBarcode(barcode)

                if (response.status == 1 && response.product != null) {
                    val product = response.product
                    val rawProductName = product.productName ?: product.genericName ?: barcode
                    val cleanedProductName = normalizeBarcodeProductName(rawProductName, product.brands)

                    _uiState.update {
                        it.copy(
                            scanResult = ScanResult(
                                productName = cleanedProductName,
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
        if (!_uiState.value.isScanning) return
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
        if (!_uiState.value.isScanning) return
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
        _overlayData.value = AnalyzerOverlayData()
        barcodeInFlight.set(false)
    }

    /**
     * Consumes the current scan result without resuming scanning.
     *
     * This prevents analyzer callbacks from producing a new scan result while the app is navigating away.
     */
    fun consumeScanResult() {
        _uiState.update { it.copy(scanResult = null) }
    }

    /**
     * Resumes scanning if the camera screen is visible again and no result is pending.
     */
    fun onCameraScreenResumed() {
        val state = _uiState.value
        if (state.scanResult == null && !state.isScanning) {
            _uiState.update { it.copy(detectedItems = emptyList(), isScanning = true) }
            _overlayData.value = AnalyzerOverlayData()
            barcodeInFlight.set(false)
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

    private fun updateOverlayLabels(labels: List<Pair<String, Float>>) {
        val top = labels
            .sortedByDescending { it.second }
            .take(3)
            .map { (name, confidence) ->
                ScannerHudLabel(name = name, confidence = confidence.coerceIn(0f, 1f))
            }

        _overlayData.update { it.copy(labels = top) }
    }

    private fun updateOverlayFromDish(
        dishName: String,
        confidence: Float,
        related: List<Pair<String, Float>>
    ) {
        val primary = ScannerHudLabel(name = dishName, confidence = confidence.coerceIn(0f, 1f))
        val secondary = related
            .filterNot { it.first.equals(dishName, ignoreCase = true) }
            .sortedByDescending { it.second }
            .take(2)
            .map { (name, conf) -> ScannerHudLabel(name = name, confidence = conf.coerceIn(0f, 1f)) }

        _overlayData.update {
            it.copy(labels = listOf(primary) + secondary)
        }
    }

    private fun updateOverlayFromBarcode(result: AnalyzerResult.BarcodeResult) {
        val bbox = result.boundingBox
        val w = result.imageWidth
        val h = result.imageHeight
        if (bbox == null || w == null || h == null) {
            _overlayData.update { it.copy(boxes = emptyList()) }
            return
        }

        val now = System.currentTimeMillis()
        val newBox = ScannerHudBox(
            imageRect = bbox.toComposeRect(),
            imageWidth = w,
            imageHeight = h,
            label = "barcode",
            confidence = null,
            isMirrored = false
        )

        // Throttle tiny movements to avoid noisy updates.
        val previous = _overlayData.value
        val prevBox = previous.boxes.firstOrNull()
        if (
            prevBox != null &&
            previous.lastBarcodeValue == result.barcode &&
            now - previous.lastBarcodeUpdateMs < 100 &&
            rectDeltaWithin(prevBox.imageRect, newBox.imageRect, deltaPx = 8f)
        ) {
            return
        }

        _overlayData.update {
            it.copy(
                boxes = listOf(newBox),
                lastBarcodeValue = result.barcode,
                lastBarcodeUpdateMs = now
            )
        }
    }
}

private data class AnalyzerOverlayData(
    val labels: List<ScannerHudLabel> = emptyList(),
    val boxes: List<ScannerHudBox> = emptyList(),
    val lastBarcodeValue: String? = null,
    val lastBarcodeUpdateMs: Long = 0L
)

private fun BoundingBox.toComposeRect(): Rect {
    return Rect(
        left = left.toFloat(),
        top = top.toFloat(),
        right = right.toFloat(),
        bottom = bottom.toFloat()
    )
}

private fun rectDeltaWithin(a: Rect, b: Rect, deltaPx: Float): Boolean {
    return (kotlin.math.abs(a.left - b.left) <= deltaPx) &&
        (kotlin.math.abs(a.top - b.top) <= deltaPx) &&
        (kotlin.math.abs(a.right - b.right) <= deltaPx) &&
        (kotlin.math.abs(a.bottom - b.bottom) <= deltaPx)
}

private fun normalizeBarcodeProductName(raw: String, brand: String? = null): String {
    // Keep a copy for debugging if you want
    var s = raw.trim().lowercase()

    // Remove bracketed noise
    s = s.replace(Regex("""[\(\[].*?[\)\]]"""), " ")

    // Remove quantities
    s = s.replace(
        Regex("""\b\d+(\.\d+)?\s?(g|kg|ml|l|oz|lb|pcs|pc|pack|packs)\b""", RegexOption.IGNORE_CASE),
        " "
    )

    // Normalize separators/whitespace early
    s = s.replace(Regex("""[|•·]"""), " ")
    s = s.replace(Regex("""\s+"""), " ").trim()

    // Remove brand if provided (OpenFoodFacts sometimes has "Brand1, Brand2")
    brand?.lowercase()?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }?.forEach { b ->
        if (s.startsWith(b + " ")) s = s.removePrefix(b).trim()
    }

    // Cut off descriptors like "in tomato sauce", "with cheese", etc.
    s = s.replace(Regex("""\s+(in|with)\s+.*$"""), "")

    // Remove packaging words
    s = s.replace(Regex("""\b(can|tin|bottle|jar|pouch|sachet)\b"""), "")
        .replace(Regex("""\s+"""), " ")
        .trim()

    return s
}