package com.fulvio.lemiericette.domain.model

import kotlinx.serialization.Serializable

/**
 * Valori nutrizionali opzionali per porzione di ricetta.
 */
@Serializable
data class Nutrition(
    val calories: Int? = null,
    val proteinGrams: Double? = null,
    val carbsGrams: Double? = null,
    val fatGrams: Double? = null,
    val sugarGrams: Double? = null,
    val saltGrams: Double? = null
)
