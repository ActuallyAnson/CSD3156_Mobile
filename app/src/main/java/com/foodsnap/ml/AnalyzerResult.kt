package com.foodsnap.ml

/**
 * Sealed class representing different types of ML analysis results.
 *
 * Used to communicate results from ML Kit analyzers to the UI layer.
 */
sealed class AnalyzerResult {
    /**
     * Result from barcode scanning.
     *
     * @property barcode The scanned barcode value
     * @property format The barcode format (e.g., EAN_13, QR_CODE)
     */
    data class BarcodeResult(
        val barcode: String,
        val format: String,
        val boundingBox: BoundingBox? = null,
        val imageWidth: Int? = null,
        val imageHeight: Int? = null,
        val rotationDegrees: Int? = null
    ) : AnalyzerResult()

    /**
     * Result from image labeling (ingredient recognition).
     *
     * @property labels List of detected labels with confidence scores
     */
    data class ImageLabelResult(
        val labels: List<DetectedLabel>
    ) : AnalyzerResult()

    /**
     * Result from dish recognition.
     *
     * @property dishName Recognized dish name
     * @property confidence Confidence score (0-1)
     * @property relatedLabels Additional labels detected
     */
    data class DishRecognitionResult(
        val dishName: String,
        val confidence: Float,
        val relatedLabels: List<DetectedLabel> = emptyList()
    ) : AnalyzerResult()

    /**
     * Error result when analysis fails.
     *
     * @property message Error description
     * @property exception The exception that occurred
     */
    data class Error(
        val message: String,
        val exception: Exception? = null
    ) : AnalyzerResult()

    /**
     * No result (nothing detected).
     */
    object Empty : AnalyzerResult()
}

/**
 * Data class representing a detected label from image analysis.
 *
 * @property text The label text
 * @property confidence Confidence score (0-1)
 * @property index Label index in the model's label map
 */
data class DetectedLabel(
    val text: String,
    val confidence: Float,
    val index: Int = -1
)

/**
 * Simple bounding box container independent of Android framework types.
 *
 * Coordinates are in image pixel space with origin at top-left.
 */
data class BoundingBox(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)
