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
 * Camera image analyzer for dish/prepared food recognition.
 *
 * Uses ML Kit Image Labeling to identify prepared dishes and meals,
 * then maps them to searchable recipe queries.
 *
 * Features:
 * - Dish/meal identification
 * - Recipe query generation
 * - Food category detection
 */
class DishRecognitionAnalyzer @Inject constructor() : ImageAnalysis.Analyzer {

    private var onDishRecognized: ((AnalyzerResult.DishRecognitionResult) -> Unit)? = null
    private var onError: ((AnalyzerResult.Error) -> Unit)? = null

    private val options = ImageLabelerOptions.Builder()
        .setConfidenceThreshold(0.6f)
        .build()

    private val labeler = ImageLabeling.getClient(options)

    // Cooldown to prevent rapid-fire callbacks
    private var lastAnalysisTime = 0L
    private val analysisCooldownMs = 1000L  // 1 second between analyses

    // Map of detected labels to dish names for recipe search
    private val dishMappings = mapOf(
        "pizza" to "pizza",
        "burger" to "burger",
        "hamburger" to "burger",
        "sandwich" to "sandwich",
        "salad" to "salad",
        "soup" to "soup",
        "pasta" to "pasta",
        "noodles" to "noodles",
        "sushi" to "sushi",
        "steak" to "steak",
        "chicken" to "chicken",
        "fish" to "fish",
        "seafood" to "seafood",
        "rice" to "rice dish",
        "curry" to "curry",
        "taco" to "taco",
        "burrito" to "burrito",
        "cake" to "cake",
        "pie" to "pie",
        "cookie" to "cookie",
        "bread" to "bread",
        "pancake" to "pancakes",
        "waffle" to "waffles",
        "eggs" to "eggs",
        "breakfast" to "breakfast",
        "dessert" to "dessert",
        "ice cream" to "ice cream",
        "smoothie" to "smoothie"
    )

    /**
     * Sets the callback for dish recognition.
     *
     * @param callback Function to call when a dish is recognized
     */
    fun setOnDishRecognizedListener(callback: (AnalyzerResult.DishRecognitionResult) -> Unit) {
        onDishRecognized = callback
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
                val allLabels = labels.map { label ->
                    DetectedLabel(
                        text = label.text,
                        confidence = label.confidence,
                        index = label.index
                    )
                }

                // Find the best matching dish from explicit mappings only
                val dishMatch = findBestDishMatch(labels.map { it.text.lowercase() to it.confidence })

                if (dishMatch != null) {
                    Log.d(TAG, "Dish recognized: ${dishMatch.first} (confidence: ${dishMatch.second})")
                    onDishRecognized?.invoke(
                        AnalyzerResult.DishRecognitionResult(
                            dishName = dishMatch.first,
                            confidence = dishMatch.second,
                            relatedLabels = allLabels
                        )
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Dish recognition failed", exception)
                onError?.invoke(
                    AnalyzerResult.Error(
                        message = "Failed to recognize dish: ${exception.message}",
                        exception = exception as? Exception
                    )
                )
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    /**
     * Finds the best matching dish from detected labels.
     *
     * @param labelConfidences List of (label, confidence) pairs
     * @return Pair of (dish name, confidence) or null if no match
     */
    private fun findBestDishMatch(labelConfidences: List<Pair<String, Float>>): Pair<String, Float>? {
        var bestMatch: Pair<String, Float>? = null

        for ((label, confidence) in labelConfidences) {
            // Only match against explicit dish mappings — avoids generic "food"/"dish" labels
            dishMappings.entries.forEach { (keyword, dishName) ->
                if (label.contains(keyword) && (bestMatch == null || confidence > bestMatch!!.second)) {
                    bestMatch = dishName to confidence
                }
            }
        }

        return bestMatch
    }

    /**
     * Releases labeler resources.
     */
    fun close() {
        labeler.close()
    }

    companion object {
        private const val TAG = "DishRecognitionAnalyzer"
    }
}
