package com.foodsnap.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Data class representing a recipe category.
 *
 * @property id Unique identifier for the category
 * @property name Display name of the category
 * @property apiValue Value used when making API calls (null for "All")
 */
data class Category(
    val id: String,
    val name: String,
    val apiValue: String? = null
)

/**
 * Default categories for recipe filtering.
 */
val defaultCategories = listOf(
    Category("all", "All", null),
    Category("breakfast", "Breakfast", "breakfast"),
    Category("lunch", "Lunch", "lunch"),
    Category("dinner", "Dinner", "main course"),
    Category("dessert", "Dessert", "dessert"),
    Category("snack", "Snack", "snack"),
    Category("appetizer", "Appetizer", "appetizer"),
    Category("salad", "Salad", "salad"),
    Category("soup", "Soup", "soup"),
    Category("beverage", "Beverage", "beverage")
)

/**
 * Horizontally scrollable row of filter chips for category selection.
 *
 * @param categories List of categories to display
 * @param selectedCategory Currently selected category ID
 * @param onCategorySelected Callback when a category is selected
 * @param modifier Optional modifier for styling
 */
@Composable
fun CategoryChips(
    categories: List<Category> = defaultCategories,
    selectedCategory: String,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            FilterChip(
                selected = category.id == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}
