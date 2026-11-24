package com.fulvio.lemiericette.data.local.converter

import androidx.room.TypeConverter
import com.fulvio.lemiericette.domain.model.Ingredient
import com.fulvio.lemiericette.domain.model.Nutrition
import com.fulvio.lemiericette.domain.model.Step
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * TypeConverters per la serializzazione di liste e oggetti complessi in Room.
 */
class Converters {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @TypeConverter
    fun fromIngredientList(value: List<Ingredient>?): String {
        return json.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toIngredientList(value: String?): List<Ingredient> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString(value)
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromStepList(value: List<Step>?): String {
        return json.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toStepList(value: String?): List<Step> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString(value)
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromNutrition(value: Nutrition?): String? {
        if (value == null) return null
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toNutrition(value: String?): Nutrition? {
        if (value.isNullOrBlank()) return null
        return try {
            json.decodeFromString(value)
        } catch (_: Exception) {
            null
        }
    }
}
