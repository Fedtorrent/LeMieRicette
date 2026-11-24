package com.fulvio.lemiericette.presentation.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fulvio.lemiericette.core.util.ImageCleanupUtils
import com.fulvio.lemiericette.core.util.WebSessionManager
import com.fulvio.lemiericette.data.local.backup.LocalZipBackupManager
import com.fulvio.lemiericette.data.local.preferences.UserPreferencesRepository
import com.fulvio.lemiericette.data.local.rtk.RtkImporter
import com.fulvio.lemiericette.data.remote.drive.GoogleDriveManager
import com.fulvio.lemiericette.data.remote.drive.GoogleDriveSyncRepository
import com.fulvio.lemiericette.data.remote.work.SyncScheduler
import com.fulvio.lemiericette.domain.model.AppFontStyle
import com.fulvio.lemiericette.domain.model.SyncState
import com.fulvio.lemiericette.domain.model.ThemeMode
import com.fulvio.lemiericette.domain.repository.RecipeRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class SettingsUiState(
    val accountName: String? = null,
    val accountEmail: String? = null,
    val isSignedIn: Boolean = false,
    val isAutoSyncEnabled: Boolean = false,
    val isImportingRtk: Boolean = false,
    val isExportingZip: Boolean = false,
    val isImportingZip: Boolean = false,
    val isKeepScreenOnEnabled: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val appFontStyle: AppFontStyle = AppFontStyle.HANDWRITTEN,
    val syncState: SyncState = SyncState.Idle,
    val lastSyncFormatted: String? = null,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val driveSyncRepository: GoogleDriveSyncRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    val currentThemeMode: StateFlow<ThemeMode> = userPreferencesRepository.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeMode.SYSTEM
    )

    val currentAppFontStyle: StateFlow<AppFontStyle> = userPreferencesRepository.appFontStyle.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppFontStyle.HANDWRITTEN
    )

    init {
        viewModelScope.launch {
            userPreferencesRepository.themeMode.collect { mode ->
                _state.update { it.copy(themeMode = mode) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.appFontStyle.collect { style ->
                _state.update { it.copy(appFontStyle = style) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.keepScreenOn.collect { keepOn ->
                _state.update { it.copy(isKeepScreenOnEnabled = keepOn) }
            }
        }
    }

    fun onThemeModeChanged(mode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(mode)
        }
    }

    fun onAppFontStyleChanged(style: AppFontStyle) {
        viewModelScope.launch {
            userPreferencesRepository.setAppFontStyle(style)
        }
    }

    fun onKeepScreenOnToggled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setKeepScreenOn(enabled)
        }
    }

    fun checkGoogleSignInStatus(context: Context) {
        val account = GoogleDriveManager.getLastSignedInAccount(context)
        updateAccountState(account)
    }

    fun onGoogleSignInResult(account: GoogleSignInAccount?) {
        if (account != null) {
            updateAccountState(account)
            _state.update { it.copy(message = "Accesso eseguito con ${account.email}") }
        } else {
            _state.update {
                it.copy(
                    isSignedIn = false,
                    accountName = null,
                    accountEmail = null,
                    message = "Accesso Google fallito o annullato"
                )
            }
        }
    }

    private fun updateAccountState(account: GoogleSignInAccount?) {
        if (account != null) {
            _state.update {
                it.copy(
                    isSignedIn = true,
                    accountName = account.displayName ?: "Utente Google",
                    accountEmail = account.email ?: ""
                )
            }
        } else {
            _state.update {
                it.copy(
                    isSignedIn = false,
                    accountName = null,
                    accountEmail = null
                )
            }
        }
    }

    fun toggleAutoSync(context: Context, enabled: Boolean) {
        _state.update { it.copy(isAutoSyncEnabled = enabled) }
        if (enabled) {
            SyncScheduler.schedulePeriodicSync(context)
            _state.update { it.copy(message = "Sincronizzazione automatica attivata (ogni 24h)") }
        } else {
            SyncScheduler.cancelPeriodicSync(context)
            _state.update { it.copy(message = "Sincronizzazione automatica disattivata") }
        }
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            try {
                SyncScheduler.cancelPeriodicSync(context)
                GoogleDriveManager.getGoogleSignInClient(context).signOut()
                _state.update {
                    it.copy(
                        isSignedIn = false,
                        accountName = null,
                        accountEmail = null,
                        isAutoSyncEnabled = false,
                        syncState = SyncState.Idle,
                        message = "Disconnessione effettuata"
                    )
                }
            } catch (_: Exception) {
                _state.update { it.copy(message = "Errore durante la disconnessione") }
            }
        }
    }

    fun performBackup(context: Context) {
        val account = GoogleDriveManager.getLastSignedInAccount(context)
        if (account == null) {
            _state.update { it.copy(message = "Effettua prima l'accesso con Google") }
            return
        }

        _state.update { it.copy(syncState = SyncState.Syncing("Backup in corso su Google Drive...")) }

        viewModelScope.launch {
            try {
                val drive = GoogleDriveManager.createDriveService(context, account)
                val localRecipes = recipeRepository.getAllRecipes().first()

                val (count, _) = driveSyncRepository.performBackup(drive, localRecipes)
                val formattedTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALIAN).format(Date())

                _state.update {
                    it.copy(
                        syncState = SyncState.Success(
                            backedUpCount = count,
                            message = "Backup di $count ricette completato con successo"
                        ),
                        lastSyncFormatted = formattedTime,
                        message = "Backup completato ($count ricette)"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update {
                    it.copy(
                        syncState = SyncState.Error("Errore durante il backup: ${e.localizedMessage}", e),
                        message = "Errore durante il backup"
                    )
                }
            }
        }
    }

    fun performRestore(context: Context) {
        val account = GoogleDriveManager.getLastSignedInAccount(context)
        if (account == null) {
            _state.update { it.copy(message = "Effettua prima l'accesso con Google") }
            return
        }

        _state.update { it.copy(syncState = SyncState.Syncing("Ripristino in corso da Google Drive...")) }

        viewModelScope.launch {
            try {
                val drive = GoogleDriveManager.createDriveService(context, account)
                val localRecipes = recipeRepository.getAllRecipes().first()

                val (restoredCount, _) = driveSyncRepository.performRestoreAndMerge(
                    drive = drive,
                    localRecipes = localRecipes,
                    saveRecipeAction = { recipeRepository.insertOrUpdateRecipe(it) }
                )

                val formattedTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALIAN).format(Date())

                _state.update {
                    it.copy(
                        syncState = SyncState.Success(
                            restoredCount = restoredCount,
                            message = "Ripristinate $restoredCount ricette da Google Drive"
                        ),
                        lastSyncFormatted = formattedTime,
                        message = "Ripristino completato ($restoredCount nuove/aggiornate)"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update {
                    it.copy(
                        syncState = SyncState.Error("Errore durante il ripristino: ${e.localizedMessage}", e),
                        message = "Errore durante il ripristino"
                    )
                }
            }
        }
    }

    fun exportZipBackup(context: Context, destinationUri: Uri) {
        _state.update {
            it.copy(
                isExportingZip = true,
                message = "Esportazione backup ZIP in corso..."
            )
        }

        viewModelScope.launch {
            try {
                val localRecipes = recipeRepository.getAllRecipes().first()
                val result = LocalZipBackupManager.exportToZip(context, destinationUri, localRecipes)

                result.fold(
                    onSuccess = { stats ->
                        _state.update {
                            it.copy(
                                isExportingZip = false,
                                message = "Backup esportato con successo! (${stats.recipesCount} ricette, ${stats.imagesCount} foto)"
                            )
                        }
                    },
                    onFailure = { error ->
                        _state.update {
                            it.copy(
                                isExportingZip = false,
                                message = "Errore durante l'esportazione: ${error.localizedMessage}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update {
                    it.copy(
                        isExportingZip = false,
                        message = "Errore durante l'esportazione: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun importZipBackup(context: Context, sourceUri: Uri) {
        _state.update {
            it.copy(
                isImportingZip = true,
                message = "Ripristino backup ZIP in corso..."
            )
        }

        viewModelScope.launch {
            try {
                val result = LocalZipBackupManager.importFromZip(
                    context = context,
                    sourceUri = sourceUri,
                    saveRecipeAction = { recipeRepository.insertOrUpdateRecipe(it) }
                )

                _state.update {
                    it.copy(
                        isImportingZip = false,
                        message = if (result.success) {
                            "Ripristino completato con successo! (${result.importedRecipesCount} ricette importate/aggiornate, ${result.imagesCount} foto)"
                        } else {
                            result.errorMessage ?: "Errore durante il ripristino del file ZIP"
                        }
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update {
                    it.copy(
                        isImportingZip = false,
                        message = "Errore durante il ripristino: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun importRtkFile(context: Context, rtkUri: Uri) {
        _state.update {
            it.copy(
                isImportingRtk = true,
                message = "Decompressione e importazione dal file .rtk..."
            )
        }

        viewModelScope.launch {
            try {
                val result = RtkImporter.importRtkFile(
                    context = context,
                    rtkUri = rtkUri,
                    saveRecipeAction = { recipeRepository.insertOrUpdateRecipe(it) }
                )

                _state.update {
                    it.copy(
                        isImportingRtk = false,
                        message = if (result.success) {
                            "Importazione completata: ${result.importedRecipesCount} ricette salvate!"
                        } else {
                            result.errorMessage ?: "Errore durante l'importazione del file .rtk"
                        }
                    )
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                _state.update {
                    it.copy(
                        isImportingRtk = false,
                        message = "Errore durante l'importazione: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun cleanupOrphanPhotos(context: Context) {
        _state.update { it.copy(message = "Scansione e pulizia foto orfane in corso...") }
        viewModelScope.launch {
            try {
                val recipes = recipeRepository.getAllRecipes().first()
                val stats = ImageCleanupUtils.cleanupOrphanImages(context, recipes)
                _state.update {
                    it.copy(
                        message = if (stats.deletedFilesCount > 0) {
                            "Pulizia completata: eliminate ${stats.deletedFilesCount} foto orfane (${stats.freedMb} liberati)!"
                        } else {
                            "Nessuna foto orfana trovata. La galleria è già pulita!"
                        }
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update {
                    it.copy(message = "Errore durante la pulizia delle foto: ${e.localizedMessage}")
                }
            }
        }
    }

    fun resetAllTriedStatus() {
        viewModelScope.launch {
            try {
                recipeRepository.resetAllTriedStatus()
                _state.update {
                    it.copy(message = "Stato 'Provata' azzerato per tutte le ricette.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update {
                    it.copy(message = "Errore durante l'azzeramento dello stato: ${e.localizedMessage}")
                }
            }
        }
    }

    fun resetDatabase(context: Context) {
        viewModelScope.launch {
            try {
                recipeRepository.deleteAllRecipes()
                ImageCleanupUtils.deleteAllImages(context)
                userPreferencesRepository.setHasPopulatedSampleData(true)
                _state.update {
                    it.copy(message = "Database e foto resettati. Tutte le ricette locali sono state eliminate.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update {
                    it.copy(message = "Errore durante il reset del database: ${e.localizedMessage}")
                }
            }
        }
    }

    fun clearBrowsingData(context: Context) {
        WebSessionManager.clearNavigationData(context)
        _state.update {
            it.copy(message = "Dati di navigazione e cookie cancellati con successo")
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
}
