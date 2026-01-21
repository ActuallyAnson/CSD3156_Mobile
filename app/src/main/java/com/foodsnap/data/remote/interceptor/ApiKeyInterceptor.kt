package com.foodsnap.data.remote.interceptor

import com.foodsnap.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor that adds the Spoonacular API key to all requests.
 *
 * The API key is loaded from BuildConfig (from local.properties).
 * Adds the key as a query parameter.
 */
class ApiKeyInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        // Add API key as query parameter
        val newUrl = originalUrl.newBuilder()
            .addQueryParameter("apiKey", BuildConfig.SPOONACULAR_API_KEY)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}
