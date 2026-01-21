package com.foodsnap.data.remote.dto.spoonacular

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Detailed recipe information from /recipes/{id}/information endpoint.
 */
@JsonClass(generateAdapter = true)
data class RecipeDetailsDto(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "image") val image: String?,
    @Json(name = "imageType") val imageType: String?,
    @Json(name = "servings") val servings: Int,
    @Json(name = "readyInMinutes") val readyInMinutes: Int,
    @Json(name = "preparationMinutes") val preparationMinutes: Int?,
    @Json(name = "cookingMinutes") val cookingMinutes: Int?,
    @Json(name = "sourceUrl") val sourceUrl: String?,
    @Json(name = "sourceName") val sourceName: String?,
    @Json(name = "summary") val summary: String?,
    @Json(name = "instructions") val instructions: String?,
    @Json(name = "analyzedInstructions") val analyzedInstructions: List<AnalyzedInstructionDto>?,
    @Json(name = "extendedIngredients") val extendedIngredients: List<ExtendedIngredientDto>?,
    @Json(name = "cuisines") val cuisines: List<String>?,
    @Json(name = "dishTypes") val dishTypes: List<String>?,
    @Json(name = "diets") val diets: List<String>?,
    @Json(name = "occasions") val occasions: List<String>?,
    @Json(name = "vegetarian") val vegetarian: Boolean?,
    @Json(name = "vegan") val vegan: Boolean?,
    @Json(name = "glutenFree") val glutenFree: Boolean?,
    @Json(name = "dairyFree") val dairyFree: Boolean?,
    @Json(name = "veryHealthy") val veryHealthy: Boolean?,
    @Json(name = "cheap") val cheap: Boolean?,
    @Json(name = "veryPopular") val veryPopular: Boolean?,
    @Json(name = "sustainable") val sustainable: Boolean?,
    @Json(name = "healthScore") val healthScore: Double?,
    @Json(name = "pricePerServing") val pricePerServing: Double?,
    @Json(name = "nutrition") val nutrition: NutritionDto?,
    @Json(name = "spoonacularScore") val spoonacularScore: Double?,
    @Json(name = "aggregateLikes") val aggregateLikes: Int?
)

/**
 * Analyzed instruction steps.
 */
@JsonClass(generateAdapter = true)
data class AnalyzedInstructionDto(
    @Json(name = "name") val name: String?,
    @Json(name = "steps") val steps: List<InstructionStepDto>?
)

/**
 * Individual instruction step.
 */
@JsonClass(generateAdapter = true)
data class InstructionStepDto(
    @Json(name = "number") val number: Int,
    @Json(name = "step") val step: String,
    @Json(name = "ingredients") val ingredients: List<StepIngredientDto>?,
    @Json(name = "equipment") val equipment: List<StepEquipmentDto>?,
    @Json(name = "length") val length: StepLengthDto?
)

/**
 * Ingredient referenced in a step.
 */
@JsonClass(generateAdapter = true)
data class StepIngredientDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "localizedName") val localizedName: String?,
    @Json(name = "image") val image: String?
)

/**
 * Equipment referenced in a step.
 */
@JsonClass(generateAdapter = true)
data class StepEquipmentDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "localizedName") val localizedName: String?,
    @Json(name = "image") val image: String?
)

/**
 * Step duration.
 */
@JsonClass(generateAdapter = true)
data class StepLengthDto(
    @Json(name = "number") val number: Int,
    @Json(name = "unit") val unit: String
)

/**
 * Extended ingredient information.
 */
@JsonClass(generateAdapter = true)
data class ExtendedIngredientDto(
    @Json(name = "id") val id: Int?,
    @Json(name = "aisle") val aisle: String?,
    @Json(name = "image") val image: String?,
    @Json(name = "consistency") val consistency: String?,
    @Json(name = "name") val name: String,
    @Json(name = "nameClean") val nameClean: String?,
    @Json(name = "original") val original: String?,
    @Json(name = "originalName") val originalName: String?,
    @Json(name = "amount") val amount: Double?,
    @Json(name = "unit") val unit: String?,
    @Json(name = "meta") val meta: List<String>?,
    @Json(name = "measures") val measures: MeasuresDto?
)

/**
 * Measurement conversions.
 */
@JsonClass(generateAdapter = true)
data class MeasuresDto(
    @Json(name = "us") val us: MeasureDto?,
    @Json(name = "metric") val metric: MeasureDto?
)

/**
 * Individual measurement.
 */
@JsonClass(generateAdapter = true)
data class MeasureDto(
    @Json(name = "amount") val amount: Double,
    @Json(name = "unitShort") val unitShort: String,
    @Json(name = "unitLong") val unitLong: String
)
