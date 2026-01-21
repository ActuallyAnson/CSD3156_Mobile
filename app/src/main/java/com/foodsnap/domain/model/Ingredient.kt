package com.foodsnap.domain.model

/**
 * Domain model representing an ingredient in the catalog.
 *
 * This is the base ingredient information, separate from recipe-specific amounts.
 *
 * @property id Unique identifier
 * @property name Ingredient name
 * @property category Food category (dairy, produce, meat, etc.)
 * @property imageUrl URL of ingredient image
 * @property barcode Barcode if applicable (for packaged products)
 * @property possibleUnits List of valid measurement units
 */
data class Ingredient(
    val id: Long,
    val name: String,
    val category: String? = null,
    val imageUrl: String? = null,
    val barcode: String? = null,
    val possibleUnits: List<String> = emptyList()
)
