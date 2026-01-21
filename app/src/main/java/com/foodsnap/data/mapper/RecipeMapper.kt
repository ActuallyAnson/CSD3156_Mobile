package com.foodsnap.data.mapper

import com.foodsnap.data.local.database.entity.IngredientEntity
import com.foodsnap.data.local.database.entity.RecipeEntity
import com.foodsnap.data.local.database.entity.RecipeIngredientCrossRef
import com.foodsnap.data.local.database.relation.IngredientWithAmount
import com.foodsnap.data.remote.dto.spoonacular.ExtendedIngredientDto
import com.foodsnap.data.remote.dto.spoonacular.RecipeDetailsDto
import com.foodsnap.data.remote.dto.spoonacular.RecipeSearchResultDto
import com.foodsnap.data.remote.dto.spoonacular.RecipesByIngredientsResponseItem
import com.foodsnap.domain.model.Recipe
import com.foodsnap.domain.model.RecipeIngredient
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

/**
 * Mapper for converting between Recipe DTOs, Entities, and Domain models.
 */
object RecipeMapper {

    private val moshi = Moshi.Builder().build()
    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val stringListAdapter = moshi.adapter<List<String>>(stringListType)

    // ============== DTO to Entity ==============

    /**
     * Converts a detailed recipe DTO to an entity.
     */
    fun toEntity(dto: RecipeDetailsDto): RecipeEntity {
        val nutrition = dto.nutrition?.nutrients
        val calories = nutrition?.find { it.name == "Calories" }?.amount
        val protein = nutrition?.find { it.name == "Protein" }?.amount
        val fat = nutrition?.find { it.name == "Fat" }?.amount
        val carbs = nutrition?.find { it.name == "Carbohydrates" }?.amount

        // Extract instructions from analyzed instructions
        val instructionSteps = dto.analyzedInstructions
            ?.flatMap { it.steps ?: emptyList() }
            ?.map { it.step }
            ?: listOf(dto.instructions ?: "")

        return RecipeEntity(
            id = dto.id.toLong(),
            spoonacularId = dto.id,
            title = dto.title,
            summary = dto.summary ?: "",
            instructions = stringListAdapter.toJson(instructionSteps),
            imageUrl = dto.image,
            servings = dto.servings,
            readyInMinutes = dto.readyInMinutes,
            sourceUrl = dto.sourceUrl,
            cuisines = stringListAdapter.toJson(dto.cuisines ?: emptyList()),
            dishTypes = stringListAdapter.toJson(dto.dishTypes ?: emptyList()),
            diets = stringListAdapter.toJson(dto.diets ?: emptyList()),
            calories = calories,
            protein = protein,
            fat = fat,
            carbs = carbs
        )
    }

    /**
     * Converts a search result DTO to an entity.
     */
    fun toEntity(dto: RecipeSearchResultDto): RecipeEntity {
        val nutrition = dto.nutrition?.nutrients
        val calories = nutrition?.find { it.name == "Calories" }?.amount

        return RecipeEntity(
            id = dto.id.toLong(),
            spoonacularId = dto.id,
            title = dto.title,
            summary = dto.summary ?: "",
            imageUrl = dto.image,
            servings = dto.servings ?: 1,
            readyInMinutes = dto.readyInMinutes ?: 0,
            sourceUrl = dto.sourceUrl,
            cuisines = stringListAdapter.toJson(dto.cuisines ?: emptyList()),
            dishTypes = stringListAdapter.toJson(dto.dishTypes ?: emptyList()),
            diets = stringListAdapter.toJson(dto.diets ?: emptyList()),
            calories = calories
        )
    }

    /**
     * Converts find-by-ingredients response to entity.
     */
    fun toEntity(dto: RecipesByIngredientsResponseItem): RecipeEntity {
        return RecipeEntity(
            id = dto.id.toLong(),
            spoonacularId = dto.id,
            title = dto.title,
            imageUrl = dto.image
        )
    }

    // ============== Entity to Domain ==============

    /**
     * Converts a recipe entity to domain model.
     */
    fun toDomain(entity: RecipeEntity): Recipe {
        val cuisines = try {
            stringListAdapter.fromJson(entity.cuisines) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        val dishTypes = try {
            stringListAdapter.fromJson(entity.dishTypes) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        val diets = try {
            stringListAdapter.fromJson(entity.diets) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        val instructions = try {
            stringListAdapter.fromJson(entity.instructions ?: "[]") ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        return Recipe(
            id = entity.id,
            title = entity.title,
            summary = entity.summary,
            imageUrl = entity.imageUrl,
            readyInMinutes = entity.readyInMinutes,
            servings = entity.servings,
            instructions = instructions,
            cuisines = cuisines,
            dishTypes = dishTypes,
            diets = diets,
            calories = entity.calories?.toInt(),
            sourceUrl = entity.sourceUrl,
            arModelUrl = entity.arModelPath
        )
    }

    /**
     * Converts a recipe entity with ingredients to domain model.
     */
    fun toDomain(
        entity: RecipeEntity,
        ingredientsWithAmount: List<IngredientWithAmount>
    ): Recipe {
        val recipe = toDomain(entity)
        val ingredients = ingredientsWithAmount.map { ingredientWithAmount ->
            RecipeIngredient(
                id = ingredientWithAmount.ingredient.id,
                name = ingredientWithAmount.ingredient.name,
                amount = ingredientWithAmount.amount,
                unit = ingredientWithAmount.unit,
                original = ingredientWithAmount.original
            )
        }
        return recipe.copy(ingredients = ingredients)
    }

    // ============== Ingredient Mapping ==============

    /**
     * Converts an extended ingredient DTO to entity.
     */
    fun toIngredientEntity(dto: ExtendedIngredientDto): IngredientEntity {
        return IngredientEntity(
            id = dto.id?.toLong() ?: System.currentTimeMillis(),
            name = dto.nameClean ?: dto.name,
            image = dto.image?.let { "https://spoonacular.com/cdn/ingredients_100x100/$it" },
            aisle = dto.aisle,
            possibleUnits = "[]"
        )
    }

    /**
     * Creates a recipe-ingredient cross reference.
     */
    fun toRecipeIngredientCrossRef(
        recipeId: Long,
        dto: ExtendedIngredientDto
    ): RecipeIngredientCrossRef {
        return RecipeIngredientCrossRef(
            recipeId = recipeId,
            ingredientId = dto.id?.toLong() ?: System.currentTimeMillis(),
            amount = dto.amount ?: 0.0,
            unit = dto.unit ?: "",
            original = dto.original ?: "${dto.amount} ${dto.unit} ${dto.name}"
        )
    }
}
