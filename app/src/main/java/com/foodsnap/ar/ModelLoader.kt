package com.foodsnap.ar

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads and manages 3D models for AR display.
 *
 * Handles:
 * - Loading GLB/GLTF models from assets
 * - Model caching
 * - Model URL management based on food categories
 */
@Singleton
class ModelLoader @Inject constructor() {

    // Cache directory for downloaded models
    private var cacheDir: File? = null

    // Mapping of food categories to model files
    private val categoryModelMap = mapOf(
        "burger" to "models/burger.glb",
        "pizza" to "models/pizza.glb",
        "salad" to "models/salad.glb",
        "pasta" to "models/pasta.glb",
        "soup" to "models/soup.glb",
        "cake" to "models/cake.glb",
        "sandwich" to "models/sandwich.glb",
        "sushi" to "models/sushi.glb",
        "steak" to "models/steak.glb",
        "chicken" to "models/chicken.glb",
        "default" to "models/plate.glb"
    )

    /**
     * Initializes the model loader with cache directory.
     *
     * @param context Application context
     */
    fun initialize(context: Context) {
        cacheDir = File(context.cacheDir, "ar_models")
        if (!cacheDir!!.exists()) {
            cacheDir!!.mkdirs()
        }
    }

    /**
     * Gets the model path for a given food category.
     *
     * @param category The food category (e.g., "burger", "pizza")
     * @return Path to the model file in assets
     */
    fun getModelPathForCategory(category: String): String {
        val normalizedCategory = category.lowercase()

        // Find best matching category
        val modelPath = categoryModelMap.entries.firstOrNull { (key, _) ->
            normalizedCategory.contains(key) || key.contains(normalizedCategory)
        }?.value ?: categoryModelMap["default"]!!

        Log.d(TAG, "Model path for category '$category': $modelPath")
        return modelPath
    }

    /**
     * Checks if a model exists in assets.
     *
     * @param context Application context
     * @param modelPath Path to check
     * @return true if model exists
     */
    fun modelExistsInAssets(context: Context, modelPath: String): Boolean {
        return try {
            context.assets.open(modelPath).close()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Model not found in assets: $modelPath")
            false
        }
    }

    /**
     * Opens a model input stream from assets.
     *
     * @param context Application context
     * @param modelPath Path to the model in assets
     * @return InputStream for the model file, or null if not found
     */
    fun openModelFromAssets(context: Context, modelPath: String): InputStream? {
        return try {
            context.assets.open(modelPath)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open model from assets: $modelPath", e)
            null
        }
    }

    /**
     * Copies a model from assets to cache for faster subsequent loads.
     *
     * @param context Application context
     * @param modelPath Path to the model in assets
     * @return File path to cached model, or null if failed
     */
    suspend fun copyModelToCache(context: Context, modelPath: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = modelPath.substringAfterLast("/")
                val cacheFile = File(cacheDir, fileName)

                if (cacheFile.exists()) {
                    Log.d(TAG, "Model already cached: ${cacheFile.absolutePath}")
                    return@withContext cacheFile
                }

                context.assets.open(modelPath).use { input ->
                    FileOutputStream(cacheFile).use { output ->
                        input.copyTo(output)
                    }
                }

                Log.d(TAG, "Model copied to cache: ${cacheFile.absolutePath}")
                cacheFile
            } catch (e: Exception) {
                Log.e(TAG, "Failed to copy model to cache: $modelPath", e)
                null
            }
        }
    }

    /**
     * Gets all available model categories.
     *
     * @return List of available category names
     */
    fun getAvailableCategories(): List<String> {
        return categoryModelMap.keys.filter { it != "default" }
    }

    /**
     * Clears the model cache.
     */
    fun clearCache() {
        cacheDir?.deleteRecursively()
        cacheDir?.mkdirs()
        Log.d(TAG, "Model cache cleared")
    }

    /**
     * Gets the cache size in bytes.
     *
     * @return Total size of cached models in bytes
     */
    fun getCacheSize(): Long {
        return cacheDir?.walkTopDown()
            ?.filter { it.isFile }
            ?.map { it.length() }
            ?.sum() ?: 0L
    }

    companion object {
        private const val TAG = "ModelLoader"
    }
}
