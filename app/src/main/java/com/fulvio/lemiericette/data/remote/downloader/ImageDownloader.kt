package com.fulvio.lemiericette.data.remote.downloader

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Utility per il download delle immagini da URL remoti e il salvataggio nella memoria locale dell'app.
 */
object ImageDownloader {

    private val client by lazy { OkHttpClient.Builder().build() }

    suspend fun downloadImageToInternalStorage(context: Context, imageUrl: String?): String? {
        if (imageUrl.isNullOrBlank()) return null

        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(imageUrl).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) return@withContext null

                val responseBody = response.body ?: return@withContext null
                val directory = File(context.filesDir, "recipe_images").apply { mkdirs() }
                val fileName = "imported_${UUID.randomUUID()}.jpg"
                val destFile = File(directory, fileName)

                responseBody.byteStream().use { input ->
                    FileOutputStream(destFile).use { output ->
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
}
