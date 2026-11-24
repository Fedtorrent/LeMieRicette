package com.fulvio.lemiericette.data.local.backup

import android.content.Context
import android.net.Uri
import com.fulvio.lemiericette.data.remote.drive.RecipeBackupPayload
import com.fulvio.lemiericette.data.remote.drive.toBackupItem
import com.fulvio.lemiericette.data.remote.drive.toDomainModel
import com.fulvio.lemiericette.domain.model.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class ZipExportStats(
    val recipesCount: Int = 0,
    val imagesCount: Int = 0
)

data class ZipImportStats(
    val importedRecipesCount: Int = 0,
    val imagesCount: Int = 0,
    val success: Boolean = true,
    val errorMessage: String? = null
)

/**
 * Manager per l'esportazione e l'importazione manuale dell'intero ricettario ed immagini in formato ZIP tramite SAF.
 */
object LocalZipBackupManager {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    fun generateDefaultBackupFileName(): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.getDefault()).format(Date())
        return "lemiericette_backup_$timestamp.zip"
    }

    suspend fun exportToZip(
        context: Context,
        destinationUri: Uri,
        recipes: List<Recipe>
    ): Result<ZipExportStats> {
        return withContext(Dispatchers.IO) {
            try {
                val outputStream = context.contentResolver.openOutputStream(destinationUri)
                    ?: return@withContext Result.failure(Exception("Impossibile aprire il file di destinazione."))

                var imagesCount = 0

                ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                    // 1. Scrivi il file JSON del ricettario
                    val payload = RecipeBackupPayload(
                        exportedAt = System.currentTimeMillis(),
                        recipes = recipes.map { it.toBackupItem() }
                    )
                    val jsonString = json.encodeToString(RecipeBackupPayload.serializer(), payload)

                    zipOut.putNextEntry(ZipEntry("recipes_backup.json"))
                    zipOut.write(jsonString.toByteArray(Charsets.UTF_8))
                    zipOut.closeEntry()

                    // 2. Scrivi le immagini locali attivamente collegate alle ricette
                    val imagesDir = File(context.filesDir, "recipe_images")
                    val activeImageFileNames = recipes
                        .mapNotNull { it.imagePath }
                        .map { File(it).name.lowercase() }
                        .toSet()

                    if (imagesDir.exists() && imagesDir.isDirectory) {
                        val imageFiles = imagesDir.listFiles() ?: emptyArray()
                        for (imageFile in imageFiles) {
                            if (imageFile.isFile && imageFile.length() > 0 && imageFile.name.lowercase() in activeImageFileNames) {
                                zipOut.putNextEntry(ZipEntry("images/${imageFile.name}"))
                                FileInputStream(imageFile).use { fileIn ->
                                    fileIn.copyTo(zipOut)
                                }
                                zipOut.closeEntry()
                                imagesCount++
                            }
                        }
                    }
                }

                Result.success(ZipExportStats(recipesCount = recipes.size, imagesCount = imagesCount))
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    suspend fun importFromZip(
        context: Context,
        sourceUri: Uri,
        saveRecipeAction: suspend (Recipe) -> Unit
    ): ZipImportStats {
        return withContext(Dispatchers.IO) {
            val tempDir = File(context.cacheDir, "zip_import_${UUID.randomUUID()}").apply { mkdirs() }
            try {
                // 1. Decompressione file ZIP
                val inputStream: InputStream = context.contentResolver.openInputStream(sourceUri)
                    ?: return@withContext ZipImportStats(success = false, errorMessage = "Impossibile aprire il file selezionato.")

                unzip(inputStream, tempDir)

                // 2. Ricerca del file recipes_backup.json
                val jsonFile = findFileByName(tempDir, "recipes_backup.json")
                    ?: return@withContext ZipImportStats(success = false, errorMessage = "File di backup non valido: 'recipes_backup.json' non trovato nell'archivio.")

                val payload = json.decodeFromString<RecipeBackupPayload>(jsonFile.readText())

                // 3. Copia di tutte le immagini estratte nella cartella permanente recipe_images dell'app
                val destImagesDir = File(context.filesDir, "recipe_images").apply { mkdirs() }
                var copiedImagesCount = 0

                tempDir.walkTopDown()
                    .filter { it.isFile && it.name != jsonFile.name && (it.extension.lowercase() in listOf("jpg", "jpeg", "png", "webp")) }
                    .forEach { extractedImg ->
                        try {
                            val destImg = File(destImagesDir, extractedImg.name)
                            extractedImg.copyTo(destImg, overwrite = true)
                            copiedImagesCount++
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                // 4. Mappatura ed aggiornamento percorsi immagini per ogni ricetta
                var importedRecipesCount = 0

                for (item in payload.recipes) {
                    val updatedImagePath = if (!item.imagePath.isNullOrBlank()) {
                        val fileName = File(item.imagePath).name
                        val localFile = File(destImagesDir, fileName)
                        if (localFile.exists()) {
                            localFile.absolutePath
                        } else {
                            item.imagePath
                        }
                    } else {
                        null
                    }

                    val recipeToSave = item.copy(imagePath = updatedImagePath).toDomainModel()
                    saveRecipeAction(recipeToSave)
                    importedRecipesCount++
                }

                ZipImportStats(
                    importedRecipesCount = importedRecipesCount,
                    imagesCount = copiedImagesCount,
                    success = true
                )
            } catch (e: Exception) {
                e.printStackTrace()
                ZipImportStats(
                    success = false,
                    errorMessage = "File ZIP danneggiato o non valido: ${e.localizedMessage}"
                )
            } finally {
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

    private fun findFileByName(dir: File, fileName: String): File? {
        return dir.walkTopDown().firstOrNull { it.isFile && it.name.equals(fileName, ignoreCase = true) }
    }
}
