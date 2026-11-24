package com.fulvio.lemiericette.domain.model

/**
 * Rappresenta lo stato dell'operazione di sincronizzazione cloud su Google Drive.
 */
sealed interface SyncState {
    data object Idle : SyncState
    data class Syncing(val message: String = "Sincronizzazione in corso...") : SyncState
    data class Success(
        val lastSyncTime: Long = System.currentTimeMillis(),
        val backedUpCount: Int = 0,
        val restoredCount: Int = 0,
        val message: String = "Sincronizzazione completata con successo"
    ) : SyncState
    data class Error(val message: String, val throwable: Throwable? = null) : SyncState
}
