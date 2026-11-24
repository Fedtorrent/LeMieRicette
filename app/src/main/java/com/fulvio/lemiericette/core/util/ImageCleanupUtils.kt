package com.fulvio.lemiericette.core.util

import android.content.Context
import com.fulvio.lemiericette.domain.model.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

data class ImageCleanupStats(
    val deletedFilesCount: Int = 0,
    val freedBytes: Long = 0L
) {
    val freedMb: String
        get() = String.format(Locale.getDefault(), "%.1f MB", freedBytes / (1024.0 * 1024.0))
}

/**
 * Utility per la manutenzione ed elisione delle fotografie orfane o rimosse dal ricettario.
 */
object ImageCleanupUtils {

    suspend fun cleanupOrphanImages(context: Context, activeRecipes: List<Recipe>): ImageCleanupStats {
        return withContext(Dispatchers.IO) {
            val imagesDir = File(context.filesDir, "recipe_images")
            if (!imagesDir.exists() || !imagesDir.isDirectory) {
                return@withContext ImageCleanupStats()
            }

            // Insieme di tutti i nomi dei file immagine attivamente riferiti nel DB
            val activeImageFileNames = activeRecipes
                .mapNotNull { it.imagePath }
                .map { File(it).name.lowercase() }
                .toSet()

            var deletedCount = 0
            var freedBytes = 0L

            val files = imagesDir.listFiles() ?: emptyArray()
            for (file in files) {
                if (file.isFile) {
                    val fileNameLower = file.name.lowercase()
                    if (fileNameLower !in activeImageFileNames) {
                        val length = file.length()
                        if (file.delete()) {
                            deletedCount++
                            freedBytes += length
                        }
                    }
                }
            }

            ImageCleanupStats(deletedFilesCount = deletedCount, freedBytes = freedBytes)
        }
    }

    suspend fun deleteLocalImageFile(context: Context, imagePath: String?, activeRecipes: List<Recipe>) {
        if (imagePath.isNullOrBlank()) return
        withContext(Dispatchers.IO) {
            try {
                val fileToDelete = File(imagePath)
                val imagesDir = File(context.filesDir, "recipe_images")

                if (fileToDelete.exists() && fileToDelete.parentFile?.absolutePath == imagesDir.absolutePath) {
                    val fileNameLower = fileToDelete.name.lowercase()
                    val isUsedByOtherRecipe = activeRecipes.any { recipe ->
                        recipe.imagePath?.let { File(it).name.lowercase() == fileNameLower } == true
                    }

                    if (!isUsedByOtherRecipe) {
                        fileToDelete.delete()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteAllImages(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val imagesDir = File(context.filesDir, "recipe_images")
                if (imagesDir.exists() && imagesDir.isDirectory) {
                    imagesDir.listFiles()?.forEach { it.delete() }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
