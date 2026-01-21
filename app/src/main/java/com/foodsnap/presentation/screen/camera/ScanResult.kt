package com.foodsnap.presentation.screen.camera

/**
 * Result of a camera scan operation.
 *
 * Contains the detected information based on the scan mode:
 * - Barcode mode: productName and potentially ingredients
 * - Ingredient mode: List of detected ingredients
 * - Dish mode: recipeId of a matching recipe
 *
 * @property recipeId ID of a recognized recipe (dish mode)
 * @property ingredients List of detected ingredient names
 * @property productName Name of a scanned product (barcode mode)
 * @property confidence Confidence score of the detection (0-1)
 */
data class ScanResult(
    val recipeId: Long? = null,
    val ingredients: List<String> = emptyList(),
    val productName: String? = null,
    val confidence: Float = 0f
)
