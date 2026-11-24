package com.fulvio.lemiericette.domain.model

/**
 * Stile del carattere tipografico dell'applicazione.
 * Corsivo è lo stile predefinito al 1° avvio dell'app.
 */
enum class AppFontStyle(val displayName: String) {
    HANDWRITTEN("Corsivo"),
    STANDARD("Standard");

    companion object {
        fun fromName(name: String?): AppFontStyle {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: HANDWRITTEN
        }
    }
}
