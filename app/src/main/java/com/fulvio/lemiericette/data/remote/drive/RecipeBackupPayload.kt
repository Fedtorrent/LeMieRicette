package com.fulvio.lemiericette.data.remote.drive

import com.fulvio.lemiericette.domain.model.Nutrition
import com.fulvio.lemiericette.domain.model.Recipe
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

/**
 * Contenitore serializzabile in JSON per l'esportazione ed il backup delle ricette.
 * Gestisce la retrocompatibilità per ripristinare backup v1 (con liste strutturate) convertendoli al testo libero.
 */
@Serializable
data class RecipeBackupPayload(
    val version: Int = 2,
    val exportedAt: Long = System.currentTimeMillis(),
    val recipes: List<RecipeBackupItem> = emptyList()
)

@Serializable
data class RecipeBackupItem(
    val id: String,
    val categoryId: String,
    val title: String,
    val description: String = "",
    val imagePath: String? = null,
    val prepTimeMin: Int = 0,
    val cookTimeMin: Int = 0,
    val servings: Int = 1,
    val ingredientsText: String = "",
    val instructionsText: String = "",
    val notes: String = "",
    val nutrition: Nutrition? = null,
    val origin: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isTried: Boolean = false,
    val sourceUrl: String? = null,
    // Campi legacy per retrocompatibilità con i vecchi backup v1
    val ingredients: JsonElement? = null,
    val steps: JsonElement? = null
)

fun Recipe.toBackupItem(): RecipeBackupItem {
    return RecipeBackupItem(
        id = id,
        categoryId = categoryId,
        title = title,
        description = description,
        imagePath = imagePath,
        prepTimeMin = prepTimeMin,
        cookTimeMin = cookTimeMin,
        servings = servings,
        ingredientsText = ingredientsText,
        instructionsText = instructionsText,
        notes = notes,
        nutrition = nutrition,
        origin = origin,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isFavorite = isFavorite,
        isTried = isTried,
        sourceUrl = sourceUrl
    )
}

fun RecipeBackupItem.toDomainModel(): Recipe {
    val resolvedIngredientsText = if (ingredientsText.isNotBlank()) {
        ingredientsText
    } else {
        parseLegacyIngredientsJson(ingredients)
    }

    val resolvedInstructionsText = if (instructionsText.isNotBlank()) {
        instructionsText
    } else {
        parseLegacyStepsJson(steps)
    }

    return Recipe(
        id = id,
        categoryId = categoryId,
        title = title,
        description = description,
        imagePath = imagePath,
        prepTimeMin = prepTimeMin,
        cookTimeMin = cookTimeMin,
        servings = servings,
        ingredientsText = resolvedIngredientsText,
        instructionsText = resolvedInstructionsText,
        notes = notes,
        nutrition = nutrition,
        origin = origin,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isFavorite = isFavorite,
        isTried = isTried,
        sourceUrl = sourceUrl
    )
}

private fun parseLegacyIngredientsJson(element: JsonElement?): String {
    if (element == null) return ""
    return try {
        when (element) {
            is JsonArray -> {
                element.mapNotNull { item ->
                    when (item) {
                        is JsonObject -> {
                            val name = item["name"]?.jsonPrimitive?.content ?: item["title"]?.jsonPrimitive?.content ?: ""
                            val amount = item["amount"]?.jsonPrimitive?.content ?: item["quantity"]?.jsonPrimitive?.content ?: ""
                            val unit = item["unit"]?.jsonPrimitive?.content ?: ""
                            buildString {
                                if (amount.isNotBlank()) append("$amount ")
                                if (unit.isNotBlank()) append("$unit ")
                                append(name)
                            }.trim().ifBlank { null }
                        }
                        is JsonPrimitive -> item.content
                        else -> null
                    }
                }.filter { it.isNotBlank() }.joinToString("\n") { if (it.startsWith("-") || it.startsWith("•")) it else "- $it" }
            }
            is JsonPrimitive -> element.content
            else -> element.toString()
        }
    } catch (_: Exception) {
        ""
    }
}

private fun parseLegacyStepsJson(element: JsonElement?): String {
    if (element == null) return ""
    return try {
        when (element) {
            is JsonArray -> {
                element.mapNotNull { item ->
                    when (item) {
                        is JsonObject -> item["description"]?.jsonPrimitive?.content ?: item["text"]?.jsonPrimitive?.content ?: item["instruction"]?.jsonPrimitive?.content
                        is JsonPrimitive -> item.content
                        else -> null
                    }
                }.filter { it.isNotBlank() }.joinToString("\n\n")
            }
            is JsonPrimitive -> element.content
            else -> element.toString()
        }
    } catch (_: Exception) {
        ""
    }
}
