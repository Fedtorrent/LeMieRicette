package com.fulvio.lemiericette.domain.model

import java.util.UUID

/**
 * Modello di dominio della Ricetta con campi di testo semplice per ingredienti e preparazione.
 */
data class Recipe(
    val id: String = UUID.randomUUID().toString(),
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
    val driveFileId: String? = null
) {
    val totalTimeMin: Int
        get() = prepTimeMin + cookTimeMin
}
