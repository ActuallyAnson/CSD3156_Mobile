package com.foodsnap.di

import com.foodsnap.ml.BarcodeAnalyzer
import com.foodsnap.ml.DishRecognitionAnalyzer
import com.foodsnap.ml.ImageLabelAnalyzer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/**
 * Hilt module for camera and ML Kit dependencies.
 *
 * Provides ML analyzers scoped to ViewModels to ensure proper lifecycle management.
 */
@Module
@InstallIn(ViewModelComponent::class)
object CameraModule {

    /**
     * Provides BarcodeAnalyzer instance.
     */
    @Provides
    @ViewModelScoped
    fun provideBarcodeAnalyzer(): BarcodeAnalyzer {
        return BarcodeAnalyzer()
    }

    /**
     * Provides ImageLabelAnalyzer instance.
     */
    @Provides
    @ViewModelScoped
    fun provideImageLabelAnalyzer(): ImageLabelAnalyzer {
        return ImageLabelAnalyzer()
    }

    /**
     * Provides DishRecognitionAnalyzer instance.
     */
    @Provides
    @ViewModelScoped
    fun provideDishRecognitionAnalyzer(): DishRecognitionAnalyzer {
        return DishRecognitionAnalyzer()
    }
}
