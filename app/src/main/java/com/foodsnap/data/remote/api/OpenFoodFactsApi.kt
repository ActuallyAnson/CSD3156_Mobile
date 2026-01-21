package com.foodsnap.data.remote.api

import com.foodsnap.data.remote.dto.openfoodfacts.ProductResponse
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit interface for OpenFoodFacts API.
 *
 * Base URL: https://world.openfoodfacts.org/
 * No API key required.
 *
 * Documentation: https://wiki.openfoodfacts.org/API
 */
interface OpenFoodFactsApi {

    companion object {
        const val BASE_URL = "https://world.openfoodfacts.org/"
    }

    /**
     * Get product information by barcode.
     *
     * @param barcode The product barcode (EAN-13, UPC-A, etc.)
     * @return Product information or status 0 if not found
     */
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): ProductResponse
}
