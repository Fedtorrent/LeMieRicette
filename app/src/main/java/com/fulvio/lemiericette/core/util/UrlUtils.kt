package com.fulvio.lemiericette.core.util

import java.net.URLDecoder

/**
 * Utility per l'estrazione e lo srotolamento di URL da testo o da link di reindirizzamento (Google Redirect Notice, Facebook, Shorteners).
 */
object UrlUtils {

    private val urlRegex = Regex("""(https?://[^\s]+)""", RegexOption.IGNORE_CASE)

    /**
     * Estrae il primo URL valido da una stringa di testo condiviso e ne srotola eventuali wrapper.
     */
    fun extractUrlFromText(text: String?): String? {
        if (text.isNullOrBlank()) return null
        val match = urlRegex.find(text)
        val extracted = match?.value?.trim() ?: return null
        return unwrapRedirectUrl(extracted)
    }

    /**
     * Estrae la porzione di testo condiviso (titolo/descrizione del post o reel) rimuovendo l'URL.
     */
    fun extractTitleFromSharedText(text: String?, extractedUrl: String?): String? {
        if (text.isNullOrBlank()) return null
        var cleaned = text
        if (!extractedUrl.isNullOrBlank()) {
            cleaned = cleaned.replace(extractedUrl, "")
        }
        cleaned = cleaned.replace(Regex("""https?://[^\s]+""", RegexOption.IGNORE_CASE), "").trim()
        cleaned = cleaned.replace(Regex("""(?i)guarda questo (?:reel|video|post) su facebook:?"""), "").trim()
        cleaned = cleaned.replace(Regex("""(?i)guarda questo post:?"""), "").trim()
        cleaned = cleaned.replace(Regex("""^\s*[:|-]\s*"""), "").trim()

        if (cleaned.length > 120) {
            cleaned = cleaned.take(120).trim() + "..."
        }
        return cleaned.ifBlank { null }
    }

    /**
     * Srotola gli URL di reindirizzamento come "Google Redirect Notice" (google.com/url?q=...) o Facebook.
     */
    fun unwrapRedirectUrl(rawUrl: String?): String {
        if (rawUrl.isNullOrBlank()) return ""
        val currentUrl = rawUrl.trim()

        try {
            // Estrazione da wrapper Google: https://www.google.com/url?q=HTTPS_REAL_URL&sa=...
            if (currentUrl.contains("google.", ignoreCase = true) && currentUrl.contains("/url", ignoreCase = true)) {
                val qMatch = Regex("""[?&](?:q|url)=(https?%3A%2F%2F[^&]+|https?://[^&]+)""", RegexOption.IGNORE_CASE).find(currentUrl)
                if (qMatch != null) {
                    val rawTarget = qMatch.groupValues[1]
                    val decodedTarget = URLDecoder.decode(rawTarget, "UTF-8")
                    if (decodedTarget.startsWith("http://", ignoreCase = true) || decodedTarget.startsWith("https://", ignoreCase = true)) {
                        return unwrapRedirectUrl(decodedTarget)
                    }
                }
            }

            // Estrazione da wrapper Facebook: https://l.facebook.com/l.php?u=HTTPS_REAL_URL...
            if (currentUrl.contains("facebook.com", ignoreCase = true) && currentUrl.contains("/l.php", ignoreCase = true)) {
                val uMatch = Regex("""[?&]u=(https?%3A%2F%2F[^&]+|https?://[^&]+)""", RegexOption.IGNORE_CASE).find(currentUrl)
                if (uMatch != null) {
                    val rawTarget = uMatch.groupValues[1]
                    val decodedTarget = URLDecoder.decode(rawTarget, "UTF-8")
                    if (decodedTarget.startsWith("http://", ignoreCase = true) || decodedTarget.startsWith("https://", ignoreCase = true)) {
                        return unwrapRedirectUrl(decodedTarget)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return currentUrl
    }
}
