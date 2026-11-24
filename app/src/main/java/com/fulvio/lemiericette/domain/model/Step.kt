package com.fulvio.lemiericette.domain.model

import kotlinx.serialization.Serializable

/**
 * Singolo passaggio numerato della preparazione di una ricetta.
 */
@Serializable
data class Step(
    val order: Int,
    val description: String
)
