package com.fulvio.lemiericette.core.util

/**
 * Wrapper per il risultato delle operazioni nel layer Data / Domain.
 */
sealed interface Result<out T> {
    data class Success<out T>(val data: T) : Result<T>
    data class Error(val exception: Throwable, val message: String? = exception.localizedMessage) : Result<Nothing>
    data object Loading : Result<Nothing>
}
