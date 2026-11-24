package com.fulvio.lemiericette.data.mapper

import com.fulvio.lemiericette.data.local.entity.RecipeEntity
import com.fulvio.lemiericette.domain.model.Recipe

/**
 * Funzioni di estensione per la conversione tra l'entità di persistenza Room e il modello di Dominio.
 */
fun RecipeEntity.toDomainModel(): Recipe {
    return Recipe(
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
        sourceUrl = sourceUrl,
        driveFileId = driveFileId
    )
}

fun Recipe.toEntity(): RecipeEntity {
    return RecipeEntity(
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
        sourceUrl = sourceUrl,
        driveFileId = driveFileId
    )
}

fun List<RecipeEntity>.toDomainModelList(): List<Recipe> {
    return map { it.toDomainModel() }
}
