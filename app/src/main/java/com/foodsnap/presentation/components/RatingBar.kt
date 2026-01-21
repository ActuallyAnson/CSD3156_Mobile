package com.foodsnap.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.foodsnap.presentation.theme.RatingStarColor

/**
 * Interactive rating bar component for displaying and selecting ratings.
 *
 * @param rating Current rating value (0-5)
 * @param onRatingChange Callback when rating is changed (null for read-only)
 * @param modifier Optional modifier for styling
 * @param starSize Size of each star icon
 * @param maxRating Maximum number of stars
 */
@Composable
fun RatingBar(
    rating: Float,
    onRatingChange: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
    starSize: Dp = 24.dp,
    maxRating: Int = 5
) {
    Row(modifier = modifier) {
        for (i in 1..maxRating) {
            val icon = when {
                rating >= i -> Icons.Filled.Star
                rating >= i - 0.5f -> Icons.Filled.StarHalf
                else -> Icons.Filled.StarBorder
            }

            Icon(
                imageVector = icon,
                contentDescription = "Star $i",
                tint = RatingStarColor,
                modifier = Modifier
                    .size(starSize)
                    .then(
                        if (onRatingChange != null) {
                            Modifier.clickable { onRatingChange(i) }
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}

/**
 * Read-only compact rating display showing rating value with stars.
 *
 * @param rating Current rating value
 * @param modifier Optional modifier for styling
 */
@Composable
fun RatingDisplay(
    rating: Float,
    modifier: Modifier = Modifier
) {
    RatingBar(
        rating = rating,
        onRatingChange = null,
        modifier = modifier,
        starSize = 16.dp
    )
}
