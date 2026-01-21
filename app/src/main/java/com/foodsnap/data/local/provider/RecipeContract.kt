package com.foodsnap.data.local.provider

import android.net.Uri

/**
 * Contract class for the Recipe Content Provider.
 *
 * Defines URIs, columns, and MIME types for accessing recipe data
 * from external applications.
 */
object RecipeContract {

    /**
     * Content Provider authority.
     */
    const val AUTHORITY = "com.foodsnap.provider"

    /**
     * Base content URI for the provider.
     */
    val BASE_CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY")

    /**
     * Paths for different data types.
     */
    object Paths {
        const val RECIPES = "recipes"
        const val SAVED_RECIPES = "saved_recipes"
        const val INGREDIENTS = "ingredients"
    }

    /**
     * Recipe table contract.
     */
    object Recipes {
        /**
         * Content URI for recipes.
         */
        val CONTENT_URI: Uri = BASE_CONTENT_URI.buildUpon()
            .appendPath(Paths.RECIPES)
            .build()

        /**
         * MIME type for a list of recipes.
         */
        const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$AUTHORITY.${Paths.RECIPES}"

        /**
         * MIME type for a single recipe.
         */
        const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.$AUTHORITY.${Paths.RECIPES}"

        /**
         * Column names.
         */
        object Columns {
            const val ID = "_id"
            const val TITLE = "title"
            const val IMAGE_URL = "image_url"
            const val READY_IN_MINUTES = "ready_in_minutes"
            const val SERVINGS = "servings"
            const val SUMMARY = "summary"
            const val INSTRUCTIONS = "instructions"
            const val SOURCE_URL = "source_url"
            const val VEGETARIAN = "vegetarian"
            const val VEGAN = "vegan"
            const val GLUTEN_FREE = "gluten_free"
            const val DAIRY_FREE = "dairy_free"
        }

        /**
         * Default sort order.
         */
        const val DEFAULT_SORT_ORDER = "${Columns.TITLE} ASC"
    }

    /**
     * Saved recipes table contract.
     */
    object SavedRecipes {
        /**
         * Content URI for saved recipes.
         */
        val CONTENT_URI: Uri = BASE_CONTENT_URI.buildUpon()
            .appendPath(Paths.SAVED_RECIPES)
            .build()

        /**
         * MIME type for a list of saved recipes.
         */
        const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$AUTHORITY.${Paths.SAVED_RECIPES}"

        /**
         * MIME type for a single saved recipe.
         */
        const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.$AUTHORITY.${Paths.SAVED_RECIPES}"

        /**
         * Column names.
         */
        object Columns {
            const val ID = "_id"
            const val RECIPE_ID = "recipe_id"
            const val NOTES = "notes"
            const val SAVED_AT = "saved_at"
        }
    }

    /**
     * Ingredients table contract.
     */
    object Ingredients {
        /**
         * Content URI for ingredients.
         */
        val CONTENT_URI: Uri = BASE_CONTENT_URI.buildUpon()
            .appendPath(Paths.INGREDIENTS)
            .build()

        /**
         * MIME type for a list of ingredients.
         */
        const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$AUTHORITY.${Paths.INGREDIENTS}"

        /**
         * Column names.
         */
        object Columns {
            const val ID = "_id"
            const val NAME = "name"
            const val IMAGE = "image"
            const val AISLE = "aisle"
        }
    }

    /**
     * URI matcher codes.
     */
    object UriCodes {
        const val RECIPES = 100
        const val RECIPE_ID = 101
        const val SAVED_RECIPES = 200
        const val SAVED_RECIPE_ID = 201
        const val INGREDIENTS = 300
        const val INGREDIENT_ID = 301
    }

    /**
     * Builds a URI for a specific recipe by ID.
     *
     * @param id Recipe ID
     * @return URI for the recipe
     */
    fun buildRecipeUri(id: Long): Uri {
        return Recipes.CONTENT_URI.buildUpon()
            .appendPath(id.toString())
            .build()
    }

    /**
     * Builds a URI for a specific saved recipe by ID.
     *
     * @param id Saved recipe ID
     * @return URI for the saved recipe
     */
    fun buildSavedRecipeUri(id: Long): Uri {
        return SavedRecipes.CONTENT_URI.buildUpon()
            .appendPath(id.toString())
            .build()
    }

    /**
     * Gets the ID from a content URI.
     *
     * @param uri Content URI
     * @return ID as Long, or -1 if not found
     */
    fun getIdFromUri(uri: Uri): Long {
        return uri.lastPathSegment?.toLongOrNull() ?: -1L
    }
}
