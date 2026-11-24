package com.fulvio.lemiericette.data.remote.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fulvio.lemiericette.data.remote.drive.GoogleDriveManager
import com.fulvio.lemiericette.data.remote.drive.GoogleDriveSyncRepository
import com.fulvio.lemiericette.domain.repository.RecipeRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Worker in background gestito da WorkManager per la sincronizzazione automatica periodica con Google Drive.
 */
@HiltWorker
class DriveSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val recipeRepository: RecipeRepository,
    private val driveSyncRepository: GoogleDriveSyncRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val account = GoogleDriveManager.getLastSignedInAccount(applicationContext)
            ?: return Result.success() // Utente non autenticato: operazione ignorata con successo

        return try {
            val driveService = GoogleDriveManager.createDriveService(applicationContext, account)
            val localRecipes = recipeRepository.getAllRecipes().first()

            // 1. Backup ricette locali su Drive
            driveSyncRepository.performBackup(driveService, localRecipes)

            // 2. Ripristino e Merge ricette remote da Drive (Last-Write-Wins)
            driveSyncRepository.performRestoreAndMerge(
                drive = driveService,
                localRecipes = localRecipes,
                saveRecipeAction = { recipeRepository.insertOrUpdateRecipe(it) }
            )

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
