package com.fulvio.lemiericette.domain.model

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Motori di ricerca supportati per la ricerca interna delle ricette.
 */
enum class SearchEngine(val displayName: String, val searchUrlPrefix: String) {
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q="),
    GOOGLE("Google", "https://www.google.com/search?q="),
    BING("Bing", "https://www.bing.com/search?q=");

    fun buildSearchUrl(rawQuery: String): String {
        val formattedQuery = if (!rawQuery.lowercase().contains("ricetta")) {
            "ricetta $rawQuery"
        } else {
            rawQuery
        }.trim()

        val encodedQuery = URLEncoder.encode(formattedQuery, StandardCharsets.UTF_8.name())
        return "$searchUrlPrefix$encodedQuery"
    }

    companion object {
        fun fromName(name: String?): SearchEngine {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: DUCKDUCKGO
        }
    }
}
