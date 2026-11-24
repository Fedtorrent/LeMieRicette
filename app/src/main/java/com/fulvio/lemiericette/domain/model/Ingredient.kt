package com.fulvio.lemiericette.domain.model

import kotlinx.serialization.Serializable

/**
 * Singolo ingrediente appartenente a una ricetta.
 */
@Serializable
data class Ingredient(
    val amount: Double? = null,
    val unit: String = "",
    val name: String
)
