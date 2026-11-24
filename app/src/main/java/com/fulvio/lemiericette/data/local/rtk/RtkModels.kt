package com.fulvio.lemiericette.data.local.rtk

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Risultato dell'importazione di un file di backup .rtk (ZIP di My Recipe Box / RecetteTek).
 */
data class RtkImportResult(
    val importedRecipesCount: Int = 0,
    val importedCategoriesCount: Int = 0,
    val success: Boolean = true,
    val errorMessage: String? = null
)

/**
 * DTO per la deserializzazione delle Categorie da categories.json.
 */
@Serializable
data class RtkCategoryDto(
    val uuid: String? = null,
    val id: String? = null,
    val title: String? = null,
    val name: String? = null,
    val label: String? = null
) {
    val categoryId: String
        get() = uuid ?: id ?: ""

    val categoryTitle: String
        get() = title ?: name ?: label ?: ""
}

/**
 * Contenitore principale per il JSON recipes_*.json ("value": [...]).
 */
@Serializable
data class RtkRecipesContainerDto(
    val value: List<RtkRecipeDto> = emptyList(),
    val Count: Int? = null
)

/**
 * DTO per la deserializzazione delle Ricette da recipes_*.json (Schema RecetteTek / My Recipe Box).
 */
@Serializable
data class RtkRecipeDto(
    val uuid: String? = null,
    val id: String? = null,
    val categoryUuid: String? = null,
    val category_uuid: String? = null,
    val categoryId: String? = null,
    val category: String? = null,
    val categories: JsonElement? = null,
    val title: String? = null,
    val name: String? = null,
    val description: String? = null,
    val preparationTime: String? = null,
    val prepTime: String? = null,
    val prepTimeMin: String? = null,
    val cookingTime: String? = null,
    val cookTime: String? = null,
    val cookTimeMin: String? = null,
    val quantity: String? = null,
    val servings: String? = null,
    val imagePath: String? = null,
    val image_path: String? = null,
    val image: String? = null,
    val pictures: JsonElement? = null,
    val originalPicture: String? = null,
    val url: String? = null,
    val ingredients: JsonElement? = null,
    val instructions: JsonElement? = null,
    val steps: JsonElement? = null,
    val notes: String? = null,
    val nutrition: JsonElement? = null,
    val origin: String? = null,
    val favorite: Boolean? = null,
    val isFavorite: Boolean? = null
)
