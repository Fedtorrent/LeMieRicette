package com.fulvio.lemiericette.domain.model

/**
 * Modalità del Tema visivo dell'applicazione (Sistema, Chiaro, Scuro).
 */
enum class ThemeMode(val displayName: String) {
    SYSTEM("Sistema"),
    LIGHT("Chiaro"),
    DARK("Scuro");

    companion object {
        fun fromName(name: String?): ThemeMode {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: SYSTEM
        }
    }
}
