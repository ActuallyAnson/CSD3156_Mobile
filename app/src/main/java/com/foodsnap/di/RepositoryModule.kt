package com.foodsnap.di

import com.foodsnap.data.repository.CommentRepositoryImpl
import com.foodsnap.data.repository.RecipeRepositoryImpl
import com.foodsnap.data.repository.SavedRecipeRepositoryImpl
import com.foodsnap.data.repository.UserInventoryRepositoryImpl
import com.foodsnap.domain.repository.CommentRepository
import com.foodsnap.domain.repository.RecipeRepository
import com.foodsnap.domain.repository.SavedRecipeRepository
import com.foodsnap.domain.repository.UserInventoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository bindings.
 *
 * Binds repository implementations to their interfaces for dependency injection.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds RecipeRepositoryImpl to RecipeRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindRecipeRepository(
        recipeRepositoryImpl: RecipeRepositoryImpl
    ): RecipeRepository

    /**
     * Binds SavedRecipeRepositoryImpl to SavedRecipeRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindSavedRecipeRepository(
        savedRecipeRepositoryImpl: SavedRecipeRepositoryImpl
    ): SavedRecipeRepository

    /**
     * Binds UserInventoryRepositoryImpl to UserInventoryRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindUserInventoryRepository(
        userInventoryRepositoryImpl: UserInventoryRepositoryImpl
    ): UserInventoryRepository

    /**
     * Binds CommentRepositoryImpl to CommentRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindCommentRepository(
        commentRepositoryImpl: CommentRepositoryImpl
    ): CommentRepository
}
