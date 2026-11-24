package com.fulvio.lemiericette.data.remote.drive

import com.fulvio.lemiericette.di.IoDispatcher
import com.fulvio.lemiericette.domain.model.Recipe
import com.google.api.client.http.ByteArrayContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository per la gestione delle operazioni di Backup e Ripristino su Google Drive (appDataFolder).
 * Supporta la sincronizzazione retrocompatibile dei dati con testo semplice per ingredienti e passaggi.
 */
@Singleton
class GoogleDriveSyncRepository @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val BACKUP_FILE_NAME = "recipes_backup.json"

    suspend fun performBackup(
        drive: Drive,
        localRecipes: List<Recipe>
    ): Pair<Int, String> {
        return withContext(ioDispatcher) {
            val payload = RecipeBackupPayload(
                version = 2,
                exportedAt = System.currentTimeMillis(),
                recipes = localRecipes.map { it.toBackupItem() }
            )
            val jsonString = json.encodeToString(payload)
            val mediaContent = ByteArrayContent("application/json", jsonString.toByteArray(StandardCharsets.UTF_8))

            val existingFile = findBackupFile(drive)

            if (existingFile != null) {
                drive.files().update(existingFile.id, null, mediaContent).execute()
            } else {
                val fileMetadata = File().apply {
                    name = BACKUP_FILE_NAME
                    parents = Collections.singletonList("appDataFolder")
                }
                drive.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()
            }

            Pair(localRecipes.size, BACKUP_FILE_NAME)
        }
    }

    suspend fun performRestoreAndMerge(
        drive: Drive,
        localRecipes: List<Recipe>,
        saveRecipeAction: suspend (Recipe) -> Unit
    ): Pair<Int, Int> {
        return withContext(ioDispatcher) {
            val backupFile = findBackupFile(drive)
                ?: throw IllegalStateException("Nessun file di backup trovato nella cartella riservata di Google Drive.")

            val outputStream = ByteArrayOutputStream()
            drive.files().get(backupFile.id).executeMediaAndDownloadTo(outputStream)

            val jsonString = outputStream.toString(StandardCharsets.UTF_8.name())
            val payload = json.decodeFromString<RecipeBackupPayload>(jsonString)

            val localMap = localRecipes.associateBy { it.id }
            var restoredCount = 0

            for (remoteItem in payload.recipes) {
                val remoteRecipe = remoteItem.toDomainModel()
                val localRecipe = localMap[remoteRecipe.id]

                // Strategia Last-Write-Wins: salviamo se la ricetta locale manca o se la remota è più recente
                if (localRecipe == null || remoteRecipe.updatedAt > localRecipe.updatedAt) {
                    saveRecipeAction(remoteRecipe)
                    restoredCount++
                }
            }

            Pair(restoredCount, payload.recipes.size)
        }
    }

    private fun findBackupFile(drive: Drive): File? {
        val fileList = drive.files().list()
            .setSpaces("appDataFolder")
            .setQ("name = '$BACKUP_FILE_NAME' and trashed = false")
            .setFields("files(id, name, modifiedTime)")
            .execute()

        return fileList.files?.firstOrNull()
    }
}
