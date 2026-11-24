package com.fulvio.lemiericette.data.local.model

/**
 * Modello di supporto per le query aggregate Room del conteggio ricette per categoria.
 */
data class CategoryCount(
    val categoryId: String,
    val count: Int
)
