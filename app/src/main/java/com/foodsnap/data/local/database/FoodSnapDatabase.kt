package com.foodsnap.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.foodsnap.data.local.database.converter.Converters
import com.foodsnap.data.local.database.dao.CommentDao
import com.foodsnap.data.local.database.dao.IngredientDao
import com.foodsnap.data.local.database.dao.RecipeDao
import com.foodsnap.data.local.database.dao.SavedRecipeDao
import com.foodsnap.data.local.database.dao.UserIngredientDao
import com.foodsnap.data.local.database.entity.CommentEntity
import com.foodsnap.data.local.database.entity.IngredientEntity
import com.foodsnap.data.local.database.entity.RecipeEntity
import com.foodsnap.data.local.database.entity.RecipeIngredientCrossRef
import com.foodsnap.data.local.database.entity.SavedRecipeEntity
import com.foodsnap.data.local.database.entity.UserIngredientEntity

/**
 * Room database for FoodSnap application.
 *
 * Contains 6 entities:
 * - RecipeEntity: Main recipe data
 * - IngredientEntity: Ingredient catalog
 * - RecipeIngredientCrossRef: Many-to-many recipe-ingredient junction
 * - SavedRecipeEntity: User's favorite recipes
 * - UserIngredientEntity: User's pantry/inventory
 * - CommentEntity: Recipe reviews and ratings
 *
 * Database version history:
 * - Version 1: Initial schema with all 6 entities
 */
@Database(
    entities = [
        RecipeEntity::class,
        IngredientEntity::class,
        RecipeIngredientCrossRef::class,
        SavedRecipeEntity::class,
        UserIngredientEntity::class,
        CommentEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class FoodSnapDatabase : RoomDatabase() {

    /**
     * DAO for recipe operations.
     */
    abstract fun recipeDao(): RecipeDao

    /**
     * DAO for ingredient operations.
     */
    abstract fun ingredientDao(): IngredientDao

    /**
     * DAO for saved recipe operations.
     */
    abstract fun savedRecipeDao(): SavedRecipeDao

    /**
     * DAO for user ingredient operations.
     */
    abstract fun userIngredientDao(): UserIngredientDao

    /**
     * DAO for comment operations.
     */
    abstract fun commentDao(): CommentDao

    companion object {
        const val DATABASE_NAME = "foodsnap_database"
    }
}
