package com.foodsnap.util

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Collection of extension functions used throughout the app.
 */

// Flow Extensions

/**
 * Wraps a Flow emission in a Resource, handling loading and error states.
 */
fun <T> Flow<T>.asResource(): Flow<Resource<T>> {
    return this
        .map<T, Resource<T>> { Resource.Success(it) }
        .onStart { emit(Resource.Loading()) }
        .catch { emit(Resource.Error(it.message ?: "Unknown error occurred")) }
}

// String Extensions

/**
 * Capitalizes the first letter of each word in a string.
 */
fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }
}

/**
 * Truncates a string to the specified length with ellipsis.
 */
fun String.truncate(maxLength: Int): String {
    return if (length > maxLength) {
        "${take(maxLength - 3)}..."
    } else {
        this
    }
}

/**
 * Removes HTML tags from a string.
 */
fun String.stripHtml(): String {
    return replace(Regex("<[^>]*>"), "")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .trim()
}

// Date Extensions

/**
 * Formats a timestamp to a readable date string.
 */
fun Long.toFormattedDate(pattern: String = "MMM dd, yyyy"): String {
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(Date(this))
}

/**
 * Formats a timestamp to relative time (e.g., "2 hours ago").
 */
fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - this

    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
        diff < TimeUnit.HOURS.toMillis(1) -> {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
        }
        diff < TimeUnit.DAYS.toMillis(1) -> {
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            "$hours ${if (hours == 1L) "hour" else "hours"} ago"
        }
        diff < TimeUnit.DAYS.toMillis(7) -> {
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            "$days ${if (days == 1L) "day" else "days"} ago"
        }
        else -> toFormattedDate()
    }
}

/**
 * Checks if a date is within the next N days (for expiry warnings).
 */
fun Long.isWithinDays(days: Int): Boolean {
    val now = System.currentTimeMillis()
    val daysInMillis = TimeUnit.DAYS.toMillis(days.toLong())
    return this in now..(now + daysInMillis)
}

/**
 * Checks if a date has passed.
 */
fun Long.isPast(): Boolean {
    return this < System.currentTimeMillis()
}

// Number Extensions

/**
 * Formats an integer to a compact string (e.g., 1000 -> "1K").
 */
fun Int.toCompactString(): String {
    return when {
        this >= 1_000_000 -> String.format("%.1fM", this / 1_000_000.0)
        this >= 1_000 -> String.format("%.1fK", this / 1_000.0)
        else -> toString()
    }
}

/**
 * Formats a double to a specified number of decimal places.
 */
fun Double.format(decimals: Int = 1): String {
    return String.format("%.${decimals}f", this)
}

// Context Extensions

/**
 * Shows a short toast message.
 */
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/**
 * Shows a long toast message.
 */
fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

// List Extensions

/**
 * Safely gets an element at index or returns null.
 */
fun <T> List<T>.getOrNull(index: Int): T? {
    return if (index in indices) get(index) else null
}

/**
 * Converts a list to a comma-separated string.
 */
fun List<String>.toCommaSeparated(): String {
    return joinToString(", ")
}
