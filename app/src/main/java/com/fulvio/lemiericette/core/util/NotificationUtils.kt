package com.fulvio.lemiericette.core.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fulvio.lemiericette.MainActivity
import com.fulvio.lemiericette.R

/**
 * Utility per la creazione delle notifiche di sistema all'importazione delle ricette.
 */
object NotificationUtils {

    private const val CHANNEL_ID = "recipe_import_channel"
    private const val CHANNEL_NAME = "Importazione Ricette"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifiche di conferma dell'importazione delle ricette dal web o da Facebook"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission")
    fun showImportNotification(context: Context, recipeTitle: String) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_icon)
            .setContentTitle("Ricetta importata con successo! 🍳")
            .setContentText("\"$recipeTitle\" è stata salvata. Torna su Le Mie Ricette per visualizzarla.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("\"$recipeTitle\" è stata salvata correttamente nel tuo ricettario. Tocca qui per aprirla.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
