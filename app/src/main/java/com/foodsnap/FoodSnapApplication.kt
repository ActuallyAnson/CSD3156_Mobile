package com.foodsnap

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * FoodSnap Application class.
 *
 * This is the main entry point for the application. It initializes Hilt for dependency injection
 * and configures WorkManager for background tasks like cache cleanup and expiry notifications.
 */
@HiltAndroidApp
class FoodSnapApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
