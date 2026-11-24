package com.fulvio.lemiericette.data.local.rtk

import android.content.Context
import android.net.Uri
import com.fulvio.lemiericette.core.util.UrlUtils
import com.fulvio.lemiericette.domain.model.Category
import com.fulvio.lemiericette.domain.model.Nutrition
import com.fulvio.lemiericette.domain.model.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import java.util.zip.ZipInputStream

/**
 * Engine per la decompressione e l'importazione dei dati da file di backup .rtk (ZIP di My Recipe Box / RecetteTek).
 * Mappa ingredienti ed istruzioni come testo semplice multilinea.
 */
object RtkImporter {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    suspend fun importRtkFile(
        context: Context,
        rtkUri: Uri,
        saveRecipeAction: suspend (Recipe) -> Unit
    ): RtkImportResult {
        return withContext(Dispatchers.IO) {
            val tempDir = File(context.cacheDir, "rtk_unzip_${UUID.randomUUID()}").apply { mkdirs() }
            try {
                // 1. Apri lo stream dal provider Android ed esegui la decompressione
                val inputStream: InputStream = try {
                    context.contentResolver.openInputStream(rtkUri)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                } ?: return@withContext RtkImportResult(success = false, errorMessage = "Impossibile aprire il file di backup selezionato.")

                try {
                    unzip(inputStream, tempDir)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return@withContext RtkImportResult(success = false, errorMessage = "Errore durante la decompressione del file .rtk.")
                }

                // 2. Lettura categories.json
                val categoryUuidMap = mutableMapOf<String, String>()
                val categoriesFile = findFileByName(tempDir, "categories.json")
                var categoriesCount = 0

                if (categoriesFile != null && categoriesFile.exists()) {
                    try {
                        val categoriesJsonStr = categoriesFile.readText()
                        val categoryDtos = json.decodeFromString<List<RtkCategoryDto>>(categoriesJsonStr)
                        categoryDtos.forEach { cat ->
                            if (cat.categoryId.isNotBlank() && cat.categoryTitle.isNotBlank()) {
                                categoryUuidMap[cat.categoryId] = cat.categoryTitle
                                categoriesCount++
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // 3. Scansione ed estrazione di tutti i file recipes*.json
                val recipeFiles = tempDir.walkTopDown()
                    .filter { it.isFile && (it.name.contains("recipe", ignoreCase = true) && it.name.endsWith(".json", ignoreCase = true)) }
                    .toList()

                var importedCount = 0

                for (recipeFile in recipeFiles) {
                    try {
                        val recipesJsonStr = recipeFile.readText()
                        val rootElement = json.parseToJsonElement(recipesJsonStr)

                        val arrayToProcess: JsonArray? = when {
                            rootElement is JsonObject && rootElement.containsKey("value") -> {
                                rootElement.jsonObject["value"] as? JsonArray
                            }
                            rootElement is JsonArray -> rootElement
                            else -> null
                        }

                        if (arrayToProcess != null) {
                            for (itemElement in arrayToProcess) {
                                try {
                                    val dto = json.decodeFromJsonElement<RtkRecipeDto>(itemElement)
                                    val recipe = mapDtoToRecipe(context, dto, categoryUuidMap, tempDir)
                                    if (recipe != null) {
                                        saveRecipeAction(recipe)
                                        importedCount++
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                RtkImportResult(
                    importedRecipesCount = importedCount,
                    importedCategoriesCount = categoriesCount,
                    success = true
                )
            } catch (e: Exception) {
                e.printStackTrace()
                RtkImportResult(
                    success = false,
                    errorMessage = "Errore durante l'importazione: ${e.localizedMessage}"
                )
            } finally {
                // Pulizia directory temporanea
                tempDir.deleteRecursively()
            }
        }
    }

    private fun unzip(inputStream: InputStream, targetDir: File) {
        ZipInputStream(BufferedInputStream(inputStream)).use { zipInput ->
            var entry = zipInput.nextEntry
            while (entry != null) {
                val newFile = File(targetDir, entry.name)
                if (entry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()
                    FileOutputStream(newFile).use { output ->
                        zipInput.copyTo(output)
                    }
                }
                zipInput.closeEntry()
                entry = zipInput.nextEntry
            }
        }
    }

    private fun mapDtoToRecipe(
        context: Context,
        dto: RtkRecipeDto,
        categoryUuidMap: Map<String, String>,
        tempDir: File
    ): Recipe? {
        val title = dto.title ?: dto.name ?: return null
        if (title.isBlank()) return null

        val recipeId = dto.uuid ?: dto.id ?: UUID.randomUUID().toString()

        // Srotolamento istantaneo offline degli URL wrapper
        val rawSourceUrl = dto.url?.trim() ?: dto.origin?.trim()
        val cleanSourceUrl = UrlUtils.unwrapRedirectUrl(rawSourceUrl)

        // Mappatura Categoria
        val rawCategoryTitle = extractCategoryTitleFromDto(dto, categoryUuidMap)
        val categoryId = Category.fromId(mapToCategoryEnumId(rawCategoryTitle)).id

        // Conversione Tempi e Porzioni
        val prepTimeMin = parseTimeString(dto.preparationTime ?: dto.prepTime ?: dto.prepTimeMin)
        val cookTimeMin = parseTimeString(dto.cookingTime ?: dto.cookTime ?: dto.cookTimeMin)
        val servings = parseTimeString(dto.quantity ?: dto.servings, default = 1)

        // Immagine: Estrazione nome file da pictures / imagePath e ricerca nella cartella decompressa
        val rawImagePath = dto.imagePath ?: dto.image_path ?: dto.image ?: extractPictureFromElement(dto.pictures)
        val localImagePath = resolveImagePath(context, rawImagePath, dto.originalPicture, tempDir)

        // Parsing Ingredienti, Passaggi e Nutrizione in testo semplice
        val ingredientsText = parseIngredientsTextJson(dto.ingredients)
        val instructionsText = parseStepsTextJson(dto.instructions ?: dto.steps)
        val nutrition = parseNutritionJson(dto.nutrition)

        val isFav = dto.isFavorite ?: dto.favorite ?: false

        return Recipe(
            id = recipeId,
            categoryId = categoryId,
            title = title.trim(),
            description = dto.description?.trim() ?: "",
            imagePath = localImagePath,
            prepTimeMin = prepTimeMin,
            cookTimeMin = cookTimeMin,
            servings = if (servings > 0) servings else 1,
            ingredientsText = ingredientsText,
            instructionsText = instructionsText,
            notes = dto.notes?.trim() ?: "",
            nutrition = nutrition,
            origin = dto.origin?.trim() ?: "",
            isFavorite = isFav,
            sourceUrl = cleanSourceUrl.ifBlank { rawSourceUrl },
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun extractCategoryTitleFromDto(dto: RtkRecipeDto, categoryUuidMap: Map<String, String>): String {
        dto.categories?.let { el ->
            try {
                when (el) {
                    is JsonArray -> {
                        val first = el.firstOrNull()
                        if (first is JsonObject) {
                            val title = first["title"]?.jsonPrimitive?.content ?: first["name"]?.jsonPrimitive?.content
                            if (!title.isNullOrBlank()) return title
                        } else if (first is JsonPrimitive) {
                            if (first.content.isNotBlank()) return first.content
                        }
                    }
                    is JsonObject -> {
                        val title = el["title"]?.jsonPrimitive?.content ?: el["name"]?.jsonPrimitive?.content
                        if (!title.isNullOrBlank()) return title
                    }
                    is JsonPrimitive -> {
                        if (el.content.isNotBlank()) return el.content
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val categoryUuid = dto.categoryUuid ?: dto.category_uuid ?: dto.categoryId ?: dto.category ?: ""
        if (categoryUuid.isNotBlank()) {
            val mapped = categoryUuidMap[categoryUuid]
            if (!mapped.isNullOrBlank()) return mapped
            return categoryUuid
        }

        return ""
    }

    private fun extractPictureFromElement(element: JsonElement?): String? {
        if (element == null) return null
        return try {
            when (element) {
                is JsonArray -> {
                    val first = element.firstOrNull()
                    if (first is JsonPrimitive) first.content else first?.toString()
                }
                is JsonPrimitive -> element.content
                else -> element.toString()
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun resolveImagePath(
        context: Context,
        rawPath: String?,
        fallbackUrl: String?,
        tempDir: File
    ): String? {
        if (!rawPath.isNullOrBlank()) {
            if (rawPath.startsWith("http://", ignoreCase = true) || rawPath.startsWith("https://", ignoreCase = true)) {
                return rawPath
            }
            val fileName = File(rawPath).name.trim()
            if (fileName.isNotBlank()) {
                val matchedFile = tempDir.walkTopDown()
                    .firstOrNull { it.isFile && it.name.equals(fileName, ignoreCase = true) }

                if (matchedFile != null && matchedFile.exists()) {
                    try {
                        val destDir = File(context.filesDir, "recipe_images").apply { mkdirs() }
                        val destFile = File(destDir, "rtk_${UUID.randomUUID()}_$fileName")
                        matchedFile.copyTo(destFile, overwrite = true)
                        return destFile.absolutePath
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        if (!fallbackUrl.isNullOrBlank() && (fallbackUrl.startsWith("http://", ignoreCase = true) || fallbackUrl.startsWith("https://", ignoreCase = true))) {
            return fallbackUrl
        }

        return null
    }

    private fun parseTimeString(str: String?, default: Int = 0): Int {
        if (str.isNullOrBlank()) return default
        return str.toIntOrNull() ?: str.filter { it.isDigit() }.toIntOrNull() ?: default
    }

    private fun parseIngredientsTextJson(element: JsonElement?): String {
        if (element == null) return ""
        return try {
            when (element) {
                is JsonArray -> {
                    element.mapNotNull { item ->
                        when (item) {
                            is JsonObject -> item["name"]?.jsonPrimitive?.content ?: item["title"]?.jsonPrimitive?.content
                            is JsonPrimitive -> item.content
                            else -> null
                        }
                    }.filter { it.isNotBlank() }.joinToString("\n") { if (it.startsWith("-") || it.startsWith("•")) it else "- $it" }
                }
                is JsonPrimitive -> element.content
                else -> element.toString()
            }
        } catch (_: Exception) {
            ""
        }
    }

    private fun parseStepsTextJson(element: JsonElement?): String {
        if (element == null) return ""
        return try {
            when (element) {
                is JsonArray -> {
                    element.mapNotNull { item ->
                        when (item) {
                            is JsonObject -> item["description"]?.jsonPrimitive?.content ?: item["text"]?.jsonPrimitive?.content ?: item["instruction"]?.jsonPrimitive?.content
                            is JsonPrimitive -> item.content
                            else -> null
                        }
                    }.filter { it.isNotBlank() }.joinToString("\n\n")
                }
                is JsonPrimitive -> element.content
                else -> element.toString()
            }
        } catch (_: Exception) {
            ""
        }
    }

    private fun parseNutritionJson(element: JsonElement?): Nutrition? {
        if (element == null) return null
        return try {
            val text = if (element is JsonPrimitive) element.content else element.toString()
            if (text.isBlank()) return null

            var cal: Int? = null
            var carb: Double? = null
            var fat: Double? = null
            var sugar: Double? = null
            var protein: Double? = null
            var sodium: Double? = null

            val lines = text.split("\n", "\r\n")
            for (line in lines) {
                val lower = line.lowercase().trim()
                val number = parseNumberFromLine(line)
                when {
                    lower.contains("calor") -> cal = number?.toInt()
                    lower.contains("carboh") || lower.contains("carbo") -> carb = number
                    lower.contains("fat") || lower.contains("grass") -> fat = number
                    lower.contains("sugar") || lower.contains("zucch") -> sugar = number
                    lower.contains("protein") || lower.contains("prote") -> protein = number
                    lower.contains("sodi") || lower.contains("sale") -> sodium = number
                }
            }

            Nutrition(
                calories = cal,
                proteinGrams = protein,
                carbsGrams = carb,
                fatGrams = fat,
                sugarGrams = sugar,
                saltGrams = sodium
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseNumberFromLine(line: String): Double? {
        val regex = Regex("""([\d.,]+)""")
        val match = regex.find(line.replace(',', '.'))
        return match?.groupValues?.get(1)?.toDoubleOrNull()
    }

    private fun findFileByName(dir: File, fileName: String): File? {
        return dir.walkTopDown().firstOrNull { it.isFile && it.name.equals(fileName, ignoreCase = true) }
    }

    private fun mapToCategoryEnumId(rawTitle: String): String {
        val cleanedTitle = rawTitle.replace(Regex("""^\d+\.\s*"""), "").trim()
        val lower = cleanedTitle.lowercase()

        return when {
            lower.contains("primo") || lower.contains("primi") || lower.contains("pasta") || lower.contains("piatti") -> Category.PRIMI.id
            lower.contains("secondo") || lower.contains("secondi") || lower.contains("carne") || lower.contains("pesce") -> Category.SECONDI.id
            lower.contains("dolc") || lower.contains("dessert") -> Category.DOLCI.id
            lower.contains("antipast") -> Category.ANTIPASTI.id
            lower.contains("contorn") -> Category.CONTORNI.id
            lower.contains("unic") -> Category.PIATTI_UNICI.id
            lower.contains("pane") || lower.contains("lievitat") || lower.contains("pizz") -> Category.PANE_E_LIEVITATI.id
            lower.contains("bevand") || lower.contains("drink") -> Category.BEVANDE.id
            lower.contains("sals") || lower.contains("condiment") -> Category.SALSE_E_CONDIMENTI.id
            else -> Category.ALTRO.id
        }
    }
}
