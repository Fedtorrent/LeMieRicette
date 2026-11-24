package com.fulvio.lemiericette.domain.model

/**
 * Categorie predefinite per l'organizzazione delle ricette.
 */
enum class Category(val id: String, val displayName: String) {
    ANTIPASTI("ANTIPASTI", "Antipasti"),
    PRIMI("PRIMI", "Primi"),
    SECONDI("SECONDI", "Secondi"),
    CONTORNI("CONTORNI", "Contorni"),
    PIATTI_UNICI("PIATTI_UNICI", "Piatti Unici"),
    DOLCI("DOLCI", "Dolci"),
    BEVANDE("BEVANDE", "Bevande"),
    SALSE_E_CONDIMENTI("SALSE_E_CONDIMENTI", "Salse e Condimenti"),
    PANE_E_LIEVITATI("PANE_E_LIEVITATI", "Pane e Lievitati"),
    ALTRO("ALTRO", "Altro");

    companion object {
        fun fromId(id: String): Category {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: ALTRO
        }
    }
}
