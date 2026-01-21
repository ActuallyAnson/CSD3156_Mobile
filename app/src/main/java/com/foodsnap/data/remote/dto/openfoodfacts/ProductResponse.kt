package com.foodsnap.data.remote.dto.openfoodfacts

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response from OpenFoodFacts /api/v0/product/{barcode}.json endpoint.
 */
@JsonClass(generateAdapter = true)
data class ProductResponse(
    @Json(name = "status") val status: Int,
    @Json(name = "status_verbose") val statusVerbose: String?,
    @Json(name = "code") val code: String?,
    @Json(name = "product") val product: ProductDto?
)

/**
 * Product information from OpenFoodFacts.
 */
@JsonClass(generateAdapter = true)
data class ProductDto(
    @Json(name = "_id") val id: String?,
    @Json(name = "code") val code: String?,
    @Json(name = "product_name") val productName: String?,
    @Json(name = "product_name_en") val productNameEn: String?,
    @Json(name = "generic_name") val genericName: String?,
    @Json(name = "brands") val brands: String?,
    @Json(name = "categories") val categories: String?,
    @Json(name = "categories_tags") val categoriesTags: List<String>?,
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "image_small_url") val imageSmallUrl: String?,
    @Json(name = "image_front_url") val imageFrontUrl: String?,
    @Json(name = "image_ingredients_url") val imageIngredientsUrl: String?,
    @Json(name = "image_nutrition_url") val imageNutritionUrl: String?,
    @Json(name = "quantity") val quantity: String?,
    @Json(name = "serving_size") val servingSize: String?,
    @Json(name = "ingredients_text") val ingredientsText: String?,
    @Json(name = "ingredients_text_en") val ingredientsTextEn: String?,
    @Json(name = "allergens") val allergens: String?,
    @Json(name = "allergens_tags") val allergensTags: List<String>?,
    @Json(name = "nutriments") val nutriments: NutrimentsDto?,
    @Json(name = "nutriscore_grade") val nutriscoreGrade: String?,
    @Json(name = "nova_group") val novaGroup: Int?,
    @Json(name = "ecoscore_grade") val ecoscoreGrade: String?
)

/**
 * Nutrition information per 100g.
 */
@JsonClass(generateAdapter = true)
data class NutrimentsDto(
    @Json(name = "energy-kcal_100g") val energyKcal100g: Double?,
    @Json(name = "energy-kcal_serving") val energyKcalServing: Double?,
    @Json(name = "proteins_100g") val proteins100g: Double?,
    @Json(name = "proteins_serving") val proteinsServing: Double?,
    @Json(name = "carbohydrates_100g") val carbohydrates100g: Double?,
    @Json(name = "carbohydrates_serving") val carbohydratesServing: Double?,
    @Json(name = "sugars_100g") val sugars100g: Double?,
    @Json(name = "sugars_serving") val sugarsServing: Double?,
    @Json(name = "fat_100g") val fat100g: Double?,
    @Json(name = "fat_serving") val fatServing: Double?,
    @Json(name = "saturated-fat_100g") val saturatedFat100g: Double?,
    @Json(name = "saturated-fat_serving") val saturatedFatServing: Double?,
    @Json(name = "fiber_100g") val fiber100g: Double?,
    @Json(name = "fiber_serving") val fiberServing: Double?,
    @Json(name = "salt_100g") val salt100g: Double?,
    @Json(name = "salt_serving") val saltServing: Double?,
    @Json(name = "sodium_100g") val sodium100g: Double?,
    @Json(name = "sodium_serving") val sodiumServing: Double?
)
