package com.fulvio.lemiericette.data.remote.scraper

import com.fulvio.lemiericette.domain.model.Category
import com.fulvio.lemiericette.domain.model.Nutrition
import com.fulvio.lemiericette.domain.model.ScrapedRecipe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Engine di Scraping ed estrazione dati da pagine HTML di ricette web.
 * Estrae ingredienti ed istruzioni direttamente come testo semplice multilinea.
 */
object RecipeScraper {

    private val json = Json { ignoreUnknownKeys = true }

    fun parseHtmlToRecipe(html: String, sourceUrl: String): ScrapedRecipe {
        val doc = Jsoup.parse(html, sourceUrl)

        // Se la pagina è l'interstiziale "Avviso di reindirizzamento" di Google:
        if (doc.title().contains("reindirizzamento", ignoreCase = true) || doc.title().contains("redirect notice", ignoreCase = true)) {
            val anchorHref = doc.select("a[href^=http]").firstOrNull()?.attr("href")
            if (!anchorHref.isNullOrBlank()) {
                val unwrappedUrl = com.fulvio.lemiericette.core.util.UrlUtils.unwrapRedirectUrl(anchorHref)
                if (unwrappedUrl.isNotBlank() && unwrappedUrl != sourceUrl) {
                    return ScrapedRecipe(
                        title = "",
                        description = "",
                        sourceUrl = unwrappedUrl
                    )
                }
            }
        }

        val finalUrl = getCanonicalOrSourceUrl(doc, sourceUrl)

        // TIER 1: JSON-LD (schema.org/Recipe)
        val jsonLdRecipe = parseJsonLd(doc, finalUrl)
        if (jsonLdRecipe != null && jsonLdRecipe.title.isNotBlank() && !isInvalidTitle(jsonLdRecipe.title)) {
            return jsonLdRecipe
        }

        // TIER 2: OpenGraph & Microdata
        val ogRecipe = parseOpenGraphAndMicrodata(doc, finalUrl)
        if (ogRecipe.title.isNotBlank() && !isInvalidTitle(ogRecipe.title)) {
            return ogRecipe
        }

        // TIER 3: Euristica Jsoup generica
        val heuristic = parseHeuristicDom(doc, finalUrl)
        if (isInvalidTitle(heuristic.title)) {
            return heuristic.copy(title = "")
        }
        return heuristic
    }

    private fun isInvalidTitle(title: String): Boolean {
        val lower = title.lowercase().trim()
        return lower.isBlank() ||
                lower.contains("error | facebook") ||
                lower.contains("error facebook") ||
                lower.contains("accedi a facebook") ||
                lower.contains("log in to facebook") ||
                lower.contains("reindirizzamento") ||
                lower.contains("redirect notice") ||
                lower == "facebook"
    }

    private fun getCanonicalOrSourceUrl(doc: Document, fallbackUrl: String): String {
        val canonical = doc.select("link[rel=canonical]").attr("abs:href").trim()
        if (canonical.startsWith("http://", ignoreCase = true) || canonical.startsWith("https://", ignoreCase = true)) {
            return canonical
        }
        val ogUrl = doc.select("meta[property=og:url]").attr("content").trim()
        if (ogUrl.startsWith("http://", ignoreCase = true) || ogUrl.startsWith("https://", ignoreCase = true)) {
            return ogUrl
        }
        return fallbackUrl
    }

    private fun parseJsonLd(doc: Document, sourceUrl: String): ScrapedRecipe? {
        val scriptElements = doc.select("script[type=application/ld+json]")
        for (element in scriptElements) {
            val scriptContent = element.data()
            if (scriptContent.isBlank()) continue

            try {
                val jsonElement = json.parseToJsonElement(scriptContent)
                val recipeJson = findRecipeJsonObject(jsonElement) ?: continue

                val title = recipeJson["name"]?.jsonPrimitive?.content ?: ""
                val description = recipeJson["description"]?.jsonPrimitive?.content ?: ""
                val imageUrl = extractImageUrlFromJson(recipeJson["image"])

                val prepTimeMin = parseIsoDuration(recipeJson["prepTime"]?.jsonPrimitive?.content)
                val cookTimeMin = parseIsoDuration(recipeJson["cookTime"]?.jsonPrimitive?.content)
                val servings = parseServings(recipeJson["recipeYield"]?.jsonPrimitive?.content)

                val rawCategory = recipeJson["recipeCategory"]?.jsonPrimitive?.content ?: ""
                val categoryId = mapCategory(rawCategory, title)

                val ingredientsText = extractIngredientsTextFromJson(recipeJson["recipeIngredient"])
                val instructionsText = extractStepsTextFromJson(recipeJson["recipeInstructions"])
                val nutrition = extractNutritionFromJson(recipeJson["nutrition"]?.jsonObject)

                return ScrapedRecipe(
                    title = title,
                    description = description,
                    categoryId = categoryId,
                    imageUrl = imageUrl,
                    prepTimeMin = prepTimeMin,
                    cookTimeMin = cookTimeMin,
                    servings = servings,
                    ingredientsText = ingredientsText,
                    instructionsText = instructionsText,
                    nutrition = nutrition,
                    sourceUrl = sourceUrl
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun findRecipeJsonObject(element: JsonElement): JsonObject? {
        return when (element) {
            is JsonObject -> {
                val type = element["@type"]?.jsonPrimitive?.content
                if (type.equals("Recipe", ignoreCase = true)) {
                    element
                } else if (element.containsKey("@graph") && element["@graph"] is JsonArray) {
                    element["@graph"]!!.jsonArray.firstNotNullOfOrNull { findRecipeJsonObject(it) }
                } else null
            }
            is JsonArray -> {
                element.firstNotNullOfOrNull { findRecipeJsonObject(it) }
            }
            else -> null
        }
    }

    private fun extractImageUrlFromJson(imageElement: JsonElement?): String? {
        if (imageElement == null) return null
        return when (imageElement) {
            is JsonObject -> imageElement["url"]?.jsonPrimitive?.content
            is JsonArray -> imageElement.firstOrNull()?.jsonPrimitive?.content
            else -> imageElement.jsonPrimitive.content
        }
    }

    private fun extractIngredientsTextFromJson(ingredientElement: JsonElement?): String {
        if (ingredientElement == null) return ""

        val rawStrings = when (ingredientElement) {
            is JsonArray -> ingredientElement.mapNotNull { it.jsonPrimitive.content }
            else -> listOf(ingredientElement.jsonPrimitive.content)
        }

        return rawStrings
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString("\n") { line -> if (line.startsWith("-") || line.startsWith("•")) line else "- $line" }
    }

    private fun extractStepsTextFromJson(instructionsElement: JsonElement?): String {
        if (instructionsElement == null) return ""

        val rawList = mutableListOf<String>()

        fun extractFromElement(el: JsonElement) {
            when (el) {
                is JsonObject -> {
                    val text = el["text"]?.jsonPrimitive?.content ?: el["name"]?.jsonPrimitive?.content
                    if (!text.isNullOrBlank()) rawList.add(text)
                    if (el.containsKey("itemListElement") && el["itemListElement"] is JsonArray) {
                        el["itemListElement"]!!.jsonArray.forEach { extractFromElement(it) }
                    }
                }
                is JsonArray -> el.forEach { extractFromElement(it) }
                else -> {
                    val text = el.jsonPrimitive.content
                    if (text.isNotBlank()) rawList.add(text)
                }
            }
        }

        extractFromElement(instructionsElement)

        return rawList
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .mapIndexed { idx, text -> "${idx + 1}. $text" }
            .joinToString("\n\n")
    }

    private fun extractNutritionFromJson(nutritionObj: JsonObject?): Nutrition? {
        if (nutritionObj == null) return null
        return Nutrition(
            calories = parseNumberString(nutritionObj["calories"]?.jsonPrimitive?.content)?.toInt(),
            proteinGrams = parseNumberString(nutritionObj["proteinContent"]?.jsonPrimitive?.content),
            carbsGrams = parseNumberString(nutritionObj["carbohydrateContent"]?.jsonPrimitive?.content),
            fatGrams = parseNumberString(nutritionObj["fatContent"]?.jsonPrimitive?.content),
            sugarGrams = parseNumberString(nutritionObj["sugarContent"]?.jsonPrimitive?.content),
            saltGrams = parseNumberString(nutritionObj["sodiumContent"]?.jsonPrimitive?.content)
        )
    }

    private fun parseOpenGraphAndMicrodata(doc: Document, sourceUrl: String): ScrapedRecipe {
        val title = doc.select("meta[property=og:title]").attr("content").ifBlank {
            doc.select("meta[name=twitter:title]").attr("content").ifBlank { doc.title() }
        }

        val description = doc.select("meta[property=og:description]").attr("content").ifBlank {
            doc.select("meta[name=description]").attr("content")
        }

        val imageUrl = doc.select("meta[property=og:image]").attr("content").ifBlank {
            doc.select("meta[name=twitter:image]").attr("content")
        }

        val categoryId = mapCategory("", title)

        return ScrapedRecipe(
            title = title,
            description = description,
            categoryId = categoryId,
            imageUrl = imageUrl.ifBlank { null },
            sourceUrl = sourceUrl
        )
    }

    private fun parseHeuristicDom(doc: Document, sourceUrl: String): ScrapedRecipe {
        val title = doc.select("h1").first()?.text() ?: doc.title()
        val description = doc.select("meta[name=description]").attr("content")

        val ingredientLines = mutableListOf<String>()
        doc.select("[class*=ingredient], [id*=ingredient], ul.ingredients > li").forEach { el ->
            val text = el.text().trim()
            if (text.isNotBlank() && text.length < 150) {
                ingredientLines.add(if (text.startsWith("-") || text.startsWith("•")) text else "- $text")
            }
        }

        val stepLines = mutableListOf<String>()
        var order = 1
        doc.select("[class*=instruction], [class*=step], [class*=preparazione], ol.steps > li").forEach { el ->
            val text = el.text().trim()
            if (text.isNotBlank() && text.length > 5) {
                stepLines.add("${order++}. $text")
            }
        }

        return ScrapedRecipe(
            title = title,
            description = description,
            categoryId = mapCategory("", title),
            ingredientsText = ingredientLines.joinToString("\n"),
            instructionsText = stepLines.joinToString("\n\n"),
            sourceUrl = sourceUrl
        )
    }

    private fun parseIsoDuration(durationStr: String?): Int {
        if (durationStr.isNullOrBlank()) return 0
        return try {
            var totalMin = 0
            val hoursMatch = Regex("""(\d+)H""", RegexOption.IGNORE_CASE).find(durationStr)
            if (hoursMatch != null) {
                totalMin += hoursMatch.groupValues[1].toInt() * 60
            }
            val minMatch = Regex("""(\d+)M""", RegexOption.IGNORE_CASE).find(durationStr)
            if (minMatch != null) {
                totalMin += minMatch.groupValues[1].toInt()
            }
            if (totalMin == 0) durationStr.filter { it.isDigit() }.toIntOrNull() ?: 0 else totalMin
        } catch (_: Exception) {
            0
        }
    }

    private fun parseServings(yieldStr: String?): Int {
        if (yieldStr.isNullOrBlank()) return 0
        val digits = yieldStr.filter { it.isDigit() }
        return digits.toIntOrNull() ?: 0
    }

    private fun parseNumberString(str: String?): Double? {
        if (str.isNullOrBlank()) return null
        val digitsWithDot = str.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.')
        return digitsWithDot.toDoubleOrNull()
    }

    private fun mapCategory(rawCategory: String, title: String): String? {
        val textToSearch = "$rawCategory $title".lowercase().trim()
        if (textToSearch.isBlank()) return null

        return when {
            textToSearch.contains("dolc") || textToSearch.contains("torta") || textToSearch.contains("tiramisu") -> Category.DOLCI.id
            textToSearch.contains("primo") || textToSearch.contains("pasta") || textToSearch.contains("risotto") || textToSearch.contains("spaghetti") || textToSearch.contains("zuppa") -> Category.PRIMI.id
            textToSearch.contains("secondo") || textToSearch.contains("carne") || textToSearch.contains("pesce") || textToSearch.contains("pollo") -> Category.SECONDI.id
            textToSearch.contains("antipast") || textToSearch.contains("bruschett") -> Category.ANTIPASTI.id
            textToSearch.contains("contorn") || textToSearch.contains("insalat") -> Category.CONTORNI.id
            textToSearch.contains("pane") || textToSearch.contains("pizz") || textToSearch.contains("focaccia") -> Category.PANE_E_LIEVITATI.id
            textToSearch.contains("bevand") || textToSearch.contains("cocktail") -> Category.BEVANDE.id
            textToSearch.contains("salsa") || textToSearch.contains("sugh") -> Category.SALSE_E_CONDIMENTI.id
            else -> null
        }
    }
}
