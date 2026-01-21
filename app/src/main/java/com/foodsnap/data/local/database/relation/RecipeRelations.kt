package com.foodsnap.data.local.database.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.foodsnap.data.local.database.entity.CommentEntity
import com.foodsnap.data.local.database.entity.IngredientEntity
import com.foodsnap.data.local.database.entity.RecipeEntity
import com.foodsnap.data.local.database.entity.RecipeIngredientCrossRef

/**
 * Data class for Recipe with its Ingredients (many-to-many relationship).
 *
 * Used by Room to automatically join recipes with their ingredients
 * through the junction table.
 */
data class RecipeWithIngredients(
    @Embedded
    val recipe: RecipeEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = RecipeIngredientCrossRef::class,
            parentColumn = "recipeId",
            entityColumn = "ingredientId"
        )
    )
    val ingredients: List<IngredientEntity>
)

/**
 * Data class for Recipe with its Comments (one-to-many relationship).
 *
 * Used by Room to automatically fetch all comments for a recipe.
 */
data class RecipeWithComments(
    @Embedded
    val recipe: RecipeEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val comments: List<CommentEntity>
)

/**
 * Data class combining Recipe with both Ingredients and Comments.
 *
 * Provides a complete view of a recipe with all related data.
 * Note: Room doesn't support multiple @Relation in one class directly,
 * so this is assembled in the repository.
 */
data class RecipeWithDetails(
    val recipe: RecipeEntity,
    val ingredients: List<IngredientWithAmount>,
    val comments: List<CommentEntity>
)

/**
 * Helper class for ingredient with its amount in a recipe.
 *
 * Combines ingredient data with quantity information from the junction table.
 */
data class IngredientWithAmount(
    val ingredient: IngredientEntity,
    val amount: Double,
    val unit: String,
    val original: String
)
