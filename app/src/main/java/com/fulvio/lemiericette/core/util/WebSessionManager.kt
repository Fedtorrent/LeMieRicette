package com.fulvio.lemiericette.core.util

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView

/**
 * Utility per la gestione della sessione browser, dei cookie e della pulizia dei dati di navigazione.
 */
object WebSessionManager {

    /**
     * Configura le impostazioni ottimali di memoria e sicurezza per la WebView.
     */
    fun configureWebViewSettings(webView: WebView) {
        setupCookieManager(webView)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            // Disattiva l'accumulo di cache enorme per evitare memory leak della memoria nativa
            cacheMode = WebSettings.LOAD_NO_CACHE
            allowFileAccess = false
            allowContentAccess = false
            allowFileAccessFromFileURLs = false
            allowUniversalAccessFromFileURLs = false
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
        }
    }

    /**
     * Inizializza le impostazioni dei Cookie per la WebView.
     */
    fun setupCookieManager(webView: WebView) {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)
    }

    /**
     * Cancella la cache, i cookie, lo storage web e la cronologia di navigazione.
     */
    fun clearNavigationData(context: Context, webView: WebView? = null) {
        try {
            // Pulizia Cookie
            val cookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookies(null)
            cookieManager.flush()

            // Pulizia WebStorage & DOM Storage
            WebStorage.getInstance().deleteAllData()

            // Pulizia Cache WebView
            webView?.clearCache(true)
            webView?.clearHistory()
            webView?.clearFormData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
