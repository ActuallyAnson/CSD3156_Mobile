package com.foodsnap.ml

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import javax.inject.Inject

/**
 * Camera image analyzer for ingredient recognition using ML Kit Image Labeling.
 *
 * Identifies food items, ingredients, and general objects in the camera frame.
 * Uses the on-device base model for fast processing.
 *
 * Features:
 * - Real-time ingredient detection
 * - Confidence threshold filtering
 * - Multiple label detection
 *
 * @property onLabelsDetected Callback when labels are detected
 * @property onError Callback when an error occurs
 */
class ImageLabelAnalyzer @Inject constructor() : ImageAnalysis.Analyzer {

    private var onLabelsDetected: ((AnalyzerResult.ImageLabelResult) -> Unit)? = null
    private var onError: ((AnalyzerResult.Error) -> Unit)? = null

    private val options = ImageLabelerOptions.Builder()
        .setConfidenceThreshold(0.7f)  // Only return labels with 70%+ confidence
        .build()

    private val labeler = ImageLabeling.getClient(options)

    // Cooldown to prevent too frequent callbacks
    private var lastAnalysisTime = 0L
    private val analysisCooldownMs = 500L  // 500ms between analyses

    /**
     * Sets the callback for label detection.
     *
     * @param callback Function to call when labels are detected
     */
    fun setOnLabelsDetectedListener(callback: (AnalyzerResult.ImageLabelResult) -> Unit) {
        onLabelsDetected = callback
    }

    /**
     * Sets the callback for errors.
     *
     * @param callback Function to call when an error occurs
     */
    fun setOnErrorListener(callback: (AnalyzerResult.Error) -> Unit) {
        onError = callback
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAnalysisTime < analysisCooldownMs) {
            imageProxy.close()
            return
        }
        lastAnalysisTime = currentTime

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        labeler.process(inputImage)
            .addOnSuccessListener { labels ->
                if (labels.isNotEmpty()) {
                    val detectedLabels = labels
                        .filter { isFoodRelated(it.text) }
                        .map { label ->
                            DetectedLabel(
                                text = label.text,
                                confidence = label.confidence,
                                index = label.index
                            )
                        }

                    if (detectedLabels.isNotEmpty()) {
                        Log.d(TAG, "Labels detected: ${detectedLabels.map { it.text }}")
                        onLabelsDetected?.invoke(
                            AnalyzerResult.ImageLabelResult(labels = detectedLabels)
                        )
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Image labeling failed", exception)
                onError?.invoke(
                    AnalyzerResult.Error(
                        message = "Failed to analyze image: ${exception.message}",
                        exception = exception as? Exception
                    )
                )
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    /**
     * Filters labels to only include food-related items.
     * This helps reduce noise from non-food objects.
     */
    private fun isFoodRelated(label: String): Boolean {
        val foodKeywords = listOf(
            "food", "fruit", "vegetable", "meat", "fish", "bread", "cheese",
            "egg", "milk", "butter", "rice", "pasta", "noodle", "soup",
            "salad", "sandwich", "pizza", "burger", "chicken", "beef", "pork",
            "apple", "banana", "orange", "tomato", "potato", "carrot", "onion",
            "lettuce", "cucumber", "pepper", "mushroom", "garlic", "lemon",
            "produce", "ingredient", "grocery", "snack", "dessert", "cake",
            "cookie", "chocolate", "candy", "beverage", "drink", "juice",
            "coffee", "tea", "wine", "beer", "sauce", "spice", "herb"
        )

        val lowerLabel = label.lowercase()
        return foodKeywords.any { keyword ->
            lowerLabel.contains(keyword)
        }
    }

    /**
     * Releases labeler resources.
     */
    fun close() {
        labeler.close()
    }

    companion object {
        private const val TAG = "ImageLabelAnalyzer"
    }
}
