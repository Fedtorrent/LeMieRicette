package com.fulvio.lemiericette.domain.model

/**
 * Risultato dell'estrazione automatica di una ricetta da una pagina HTML/URL.
 * Ingredienti ed istruzioni sono memorizzati come stringhe di testo multilinea.
 */
data class ScrapedRecipe(
    val title: String = "",
    val description: String = "",
    val categoryId: String? = null,
    val imageUrl: String? = null,
    val prepTimeMin: Int = 0,
    val cookTimeMin: Int = 0,
    val servings: Int = 0,
    val ingredientsText: String = "",
    val instructionsText: String = "",
    val notes: String = "",
    val nutrition: Nutrition? = null,
    val origin: String = "",
    val sourceUrl: String = ""
)
