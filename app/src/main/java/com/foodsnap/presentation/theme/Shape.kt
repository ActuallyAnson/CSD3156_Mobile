package com.foodsnap.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * FoodSnap shape definitions.
 *
 * Custom shapes for UI elements like cards, buttons, and dialogs.
 * Uses rounded corners for a friendly, approachable feel.
 */
val Shapes = Shapes(
    // Extra small - for chips and small components
    extraSmall = RoundedCornerShape(4.dp),

    // Small - for text fields and small cards
    small = RoundedCornerShape(8.dp),

    // Medium - for cards and dialogs
    medium = RoundedCornerShape(12.dp),

    // Large - for bottom sheets and larger cards
    large = RoundedCornerShape(16.dp),

    // Extra large - for full-screen dialogs and containers
    extraLarge = RoundedCornerShape(24.dp)
)

/**
 * Custom shape values for specific UI elements.
 */
object CustomShapes {
    val RecipeCard = RoundedCornerShape(16.dp)
    val RecipeCardImage = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    val CategoryChip = RoundedCornerShape(20.dp)
    val SearchBar = RoundedCornerShape(28.dp)
    val BottomSheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val FloatingActionButton = RoundedCornerShape(16.dp)
    val Button = RoundedCornerShape(12.dp)
    val IngredientChip = RoundedCornerShape(8.dp)
    val RatingBadge = RoundedCornerShape(4.dp)
}
