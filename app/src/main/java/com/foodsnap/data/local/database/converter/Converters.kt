package com.foodsnap.data.local.database.converter

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Room type converters for complex data types.
 *
 * Handles conversion between Kotlin types and SQLite-compatible types.
 * Uses Moshi for JSON serialization of lists.
 */
class Converters {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val stringListAdapter = moshi.adapter<List<String>>(stringListType)

    /**
     * Converts a JSON string to a List of Strings.
     */
    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        if (value == null || value.isEmpty()) return emptyList()
        return try {
            stringListAdapter.fromJson(value) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Converts a List of Strings to a JSON string.
     */
    @TypeConverter
    fun toStringList(list: List<String>?): String {
        if (list == null || list.isEmpty()) return "[]"
        return try {
            stringListAdapter.toJson(list)
        } catch (e: Exception) {
            "[]"
        }
    }

}
