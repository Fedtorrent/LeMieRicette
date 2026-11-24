package com.fulvio.lemiericette.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fulvio.lemiericette.domain.model.Nutrition

/**
 * Entità Room per la tabella "recipes" con campi di testo libero per ingredienti ed istruzioni.
 */
@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey
    val id: String,
    val categoryId: String,
    val title: String,
    val description: String,
    val imagePath: String?,
    val prepTimeMin: Int,
    val cookTimeMin: Int,
    val servings: Int,
    val ingredientsText: String,
    val instructionsText: String,
    val notes: String,
    val nutrition: Nutrition?,
    val origin: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isFavorite: Boolean,
    val isTried: Boolean = false,
    val sourceUrl: String?,
    val driveFileId: String?
)
