package com.foodsnap.di

import android.content.Context
import androidx.room.Room
import com.foodsnap.data.local.database.FoodSnapDatabase
import com.foodsnap.data.local.database.dao.CommentDao
import com.foodsnap.data.local.database.dao.IngredientDao
import com.foodsnap.data.local.database.dao.RecipeDao
import com.foodsnap.data.local.database.dao.SavedRecipeDao
import com.foodsnap.data.local.database.dao.UserIngredientDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database dependencies.
 *
 * Provides the Room database instance and all DAOs.
 * All dependencies are singleton-scoped for the application lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the Room database instance.
     *
     * Uses destructive migration fallback during development.
     * TODO: Implement proper migrations for production.
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): FoodSnapDatabase {
        return Room.databaseBuilder(
            context,
            FoodSnapDatabase::class.java,
            FoodSnapDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Provides the Recipe DAO.
     */
    @Provides
    @Singleton
    fun provideRecipeDao(database: FoodSnapDatabase): RecipeDao {
        return database.recipeDao()
    }

    /**
     * Provides the Ingredient DAO.
     */
    @Provides
    @Singleton
    fun provideIngredientDao(database: FoodSnapDatabase): IngredientDao {
        return database.ingredientDao()
    }

    /**
     * Provides the Saved Recipe DAO.
     */
    @Provides
    @Singleton
    fun provideSavedRecipeDao(database: FoodSnapDatabase): SavedRecipeDao {
        return database.savedRecipeDao()
    }

    /**
     * Provides the User Ingredient DAO.
     */
    @Provides
    @Singleton
    fun provideUserIngredientDao(database: FoodSnapDatabase): UserIngredientDao {
        return database.userIngredientDao()
    }

    /**
     * Provides the Comment DAO.
     */
    @Provides
    @Singleton
    fun provideCommentDao(database: FoodSnapDatabase): CommentDao {
        return database.commentDao()
    }
}
