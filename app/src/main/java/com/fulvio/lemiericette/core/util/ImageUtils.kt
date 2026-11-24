package com.fulvio.lemiericette.core.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

/**
 * Utility per la gestione del salvataggio delle foto da Fotocamera o Galleria nella memoria interna dell'app.
 */
object ImageUtils {

    /**
     * Crea un file temporaneo e restituisce la relativa URI tramite FileProvider per lo scatto da Fotocamera.
     */
    fun createTempPictureUri(context: Context): Uri {
        val storageDir = File(context.cacheDir, "camera_photos").apply { mkdirs() }
        val file = File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_",
            ".jpg",
            storageDir
        )
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }

    /**
     * Copia l'immagine selezionata da URI (Galleria o Fotocamera) nella cartella permanente dell'app.
     * Restituisce il percorso del file salvato.
     */
    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val directory = File(context.filesDir, "recipe_images").apply { mkdirs() }
            val fileName = "recipe_${UUID.randomUUID()}.jpg"
            val destFile = File(directory, fileName)

            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(destFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
