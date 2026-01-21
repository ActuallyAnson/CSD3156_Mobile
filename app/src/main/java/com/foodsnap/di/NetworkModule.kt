package com.foodsnap.di

import android.content.Context
import com.foodsnap.data.remote.api.OpenFoodFactsApi
import com.foodsnap.data.remote.api.SpoonacularApi
import com.foodsnap.data.remote.interceptor.ApiKeyInterceptor
import com.foodsnap.util.Constants
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Hilt module for network dependencies.
 *
 * Provides Retrofit instances for both APIs, OkHttp client, and Moshi.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides Moshi instance for JSON serialization.
     */
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    /**
     * Provides HTTP cache.
     */
    @Provides
    @Singleton
    fun provideCache(@ApplicationContext context: Context): Cache {
        val cacheDir = File(context.cacheDir, "http_cache")
        val cacheSize = Constants.CACHE_SIZE_MB * 1024 * 1024 // 10 MB
        return Cache(cacheDir, cacheSize)
    }

    /**
     * Provides logging interceptor for debugging.
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * Provides API key interceptor for Spoonacular.
     */
    @Provides
    @Singleton
    fun provideApiKeyInterceptor(): ApiKeyInterceptor {
        return ApiKeyInterceptor()
    }

    /**
     * Provides OkHttp client for Spoonacular API (with API key interceptor).
     */
    @Provides
    @Singleton
    @Named("spoonacular")
    fun provideSpoonacularOkHttpClient(
        cache: Cache,
        loggingInterceptor: HttpLoggingInterceptor,
        apiKeyInterceptor: ApiKeyInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provides OkHttp client for OpenFoodFacts API (no API key needed).
     */
    @Provides
    @Singleton
    @Named("openfoodfacts")
    fun provideOpenFoodFactsOkHttpClient(
        cache: Cache,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provides Spoonacular API Retrofit instance.
     */
    @Provides
    @Singleton
    fun provideSpoonacularApi(
        @Named("spoonacular") okHttpClient: OkHttpClient,
        moshi: Moshi
    ): SpoonacularApi {
        return Retrofit.Builder()
            .baseUrl(SpoonacularApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SpoonacularApi::class.java)
    }

    /**
     * Provides OpenFoodFacts API Retrofit instance.
     */
    @Provides
    @Singleton
    fun provideOpenFoodFactsApi(
        @Named("openfoodfacts") okHttpClient: OkHttpClient,
        moshi: Moshi
    ): OpenFoodFactsApi {
        return Retrofit.Builder()
            .baseUrl(OpenFoodFactsApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenFoodFactsApi::class.java)
    }
}
