package com.fulvio.lemiericette.presentation.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DriveFolderUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fulvio.lemiericette.R
import com.fulvio.lemiericette.core.ui.components.AppTopBar
import com.fulvio.lemiericette.data.local.backup.LocalZipBackupManager
import com.fulvio.lemiericette.data.remote.drive.GoogleDriveManager
import com.fulvio.lemiericette.domain.model.AppFontStyle
import com.fulvio.lemiericette.domain.model.SyncState
import com.fulvio.lemiericette.domain.model.ThemeMode
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    var showResetTriedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.checkGoogleSignInStatus(context)
    }

    LaunchedEffect(state.message) {
        state.message?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Reset del Database") },
            text = { Text("Sei sicuro di voler eliminare TUTTE le ricette locali presenti nell'applicazione? Questa azione è irreversibile e svuoterà completamente il database.") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        viewModel.resetDatabase(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Elimina Tutto")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }

    if (showResetTriedDialog) {
        AlertDialog(
            onDismissRequest = { showResetTriedDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Azzerare lo stato 'Provata'?") },
            text = { Text("Tutte le ricette presenti nel database verranno impostate allo stato 'Da provare' (isTried = false). Vuoi proseguire?") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetTriedDialog = false
                        viewModel.resetAllTriedStatus()
                    }
                ) {
                    Text("Azzera Stato")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetTriedDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }

    // Launcher per il flusso di Google Sign-In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            viewModel.onGoogleSignInResult(account)
        } catch (e: ApiException) {
            e.printStackTrace()
            viewModel.onGoogleSignInResult(null)
        }
    }

    // Launcher per la selezione del file di backup .rtk via Storage Access Framework
    val rtkFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { selectedUri ->
            viewModel.importRtkFile(context, selectedUri)
        }
    }

    // Launcher per Esportazione Backup ZIP (SAF CreateDocument)
    val zipExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let { selectedUri ->
            viewModel.exportZipBackup(context, selectedUri)
        }
    }

    // Launcher per Importazione Backup ZIP (SAF OpenDocument)
    val zipImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { selectedUri ->
            viewModel.importZipBackup(context, selectedUri)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Impostazioni",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // 1. CARD SELEZIONE TEMA E CARATTERE TIPOGRAFICO
            CollapsibleSettingsCard(
                title = "Tema e Carattere Tipografico",
                subtitle = "Personalizza tema e stile del font (Standard/Corsivo)",
                icon = Icons.Default.Palette,
                iconTint = MaterialTheme.colorScheme.primary,
                initiallyExpanded = false
            ) {
                Text(
                    text = "Scegli l'aspetto visivo dell'app ed il tipo di carattere. Puoi passare al font Corsivo per un look da ricettario di casa!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Tema dell'app:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = state.themeMode == mode,
                            onClick = { viewModel.onThemeModeChanged(mode) },
                            label = { Text(mode.displayName) },
                            leadingIcon = {
                                if (state.themeMode == mode) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Stile del Carattere (Font):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppFontStyle.entries.forEach { fontStyle ->
                        val chipFontFamily = when (fontStyle) {
                            AppFontStyle.STANDARD -> FontFamily.Default
                            AppFontStyle.HANDWRITTEN -> FontFamily.Cursive
                        }
                        val chipFontSize = if (fontStyle == AppFontStyle.HANDWRITTEN) 18.sp else 14.sp

                        FilterChip(
                            selected = state.appFontStyle == fontStyle,
                            onClick = { viewModel.onAppFontStyleChanged(fontStyle) },
                            label = {
                                Text(
                                    text = fontStyle.displayName,
                                    fontFamily = chipFontFamily,
                                    fontSize = chipFontSize,
                                    fontWeight = if (fontStyle == AppFontStyle.STANDARD) FontWeight.Medium else FontWeight.Bold
                                )
                            },
                            leadingIcon = {
                                if (state.appFontStyle == fontStyle) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 2. CARD ALTRE IMPOSTAZIONI (SCHERMO SEMPRE ACCESO)
            CollapsibleSettingsCard(
                title = "Altre Impostazioni",
                subtitle = "Gestione dello schermo sempre acceso",
                icon = Icons.Default.Tune,
                iconTint = MaterialTheme.colorScheme.primary,
                initiallyExpanded = false
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Schermo Sempre Acceso",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Mantiene lo schermo attivo mentre consulti le ricette in cucina",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = state.isKeepScreenOnEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.onKeepScreenOnToggled(enabled)
                        }
                    )
                }
            }

            // 3. CARD BACKUP & RIPRISTINO LOCALE ZIP (SAF)
            CollapsibleSettingsCard(
                title = "Backup Locale ed Esportazione ZIP",
                subtitle = "Esporta o ripristina ricette e foto in formato ZIP",
                icon = Icons.Default.FolderZip,
                iconTint = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = "Esporta o ripristina un file archivio ZIP completo contenente tutte le ricette ed i file delle foto salvati sul dispositivo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                AnimatedVisibility(visible = state.isExportingZip || state.isImportingZip) {
                    Column {
                        val msg = if (state.isExportingZip) "Esportazione backup ZIP in corso..." else "Ripristino backup ZIP in corso..."
                        Text(msg, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val fileName = LocalZipBackupManager.generateDefaultBackupFileName()
                            zipExportLauncher.launch(fileName)
                        },
                        enabled = !state.isExportingZip && !state.isImportingZip,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.DriveFolderUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Esporta Dati")
                    }

                    OutlinedButton(
                        onClick = {
                            zipImportLauncher.launch(arrayOf("application/zip", "*/*"))
                        },
                        enabled = !state.isExportingZip && !state.isImportingZip,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Importa ZIP")
                    }
                }
            }

            // 4. CARD ACCOUNT GOOGLE & CLOUD SYNC
            CollapsibleSettingsCard(
                title = "Account Google & Cloud Sync",
                subtitle = if (state.isSignedIn) state.accountEmail ?: "Account connesso" else "Accedi per attivare il backup cloud",
                icon = Icons.Default.CloudSync,
                iconTint = MaterialTheme.colorScheme.secondary
            ) {
                if (state.isSignedIn) {
                    Column {
                        Text(
                            text = state.accountName ?: "Utente Google",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = state.accountEmail ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.signOut(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Disconnetti Account")
                    }
                } else {
                    Text(
                        text = "Accedi con il tuo account Google per attivare la sincronizzazione cloud, il backup ed il ripristino delle ricette.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            val signInIntent = GoogleDriveManager.getSignInIntent(context)
                            googleSignInLauncher.launch(signInIntent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Accedi con Google")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                if (state.lastSyncFormatted != null) {
                    Text(
                        text = "Ultima sincronizzazione: ${state.lastSyncFormatted}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                AnimatedVisibility(visible = state.syncState is SyncState.Syncing) {
                    Column {
                        val syncingMessage = (state.syncState as? SyncState.Syncing)?.message ?: "Sincronizzazione..."
                        Text(syncingMessage, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }

                if (state.syncState is SyncState.Error) {
                    val errMsg = (state.syncState as SyncState.Error).message
                    Text(
                        text = errMsg,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.performBackup(context) },
                        enabled = state.isSignedIn && state.syncState !is SyncState.Syncing,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Backup Ora")
                    }

                    OutlinedButton(
                        onClick = { viewModel.performRestore(context) },
                        enabled = state.isSignedIn && state.syncState !is SyncState.Syncing,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ripristina")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // SWITCH SINCRONIZZAZIONE AUTOMATICA WORKMANAGER IN BACKGROUND
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sincronizzazione Automatica in Background",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Esegue il backup in background (ogni 24h, solo con connessione di rete)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = state.isAutoSyncEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.toggleAutoSync(context, enabled)
                        },
                        enabled = state.isSignedIn
                    )
                }
            }

            // 5. SEZIONE BROWSER & PRIVACY
            CollapsibleSettingsCard(
                title = "Browser & Privacy",
                subtitle = "Cancella cookie, cache e dati di navigazione",
                icon = Icons.Default.DeleteSweep,
                iconTint = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = "Cancella i cookie, la cache e la cronologia salvati durante la navigazione nel browser interno.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = { viewModel.clearBrowsingData(context) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancella dati di navigazione")
                }
            }

            // 6. SOTTOMENU COLLASSABILE "IMPOSTAZIONI AVANZATE" (OPZIONI SPECIALI, MANUTENZIONE & PULIZIA)
            CollapsibleSettingsCard(
                title = "Impostazioni Avanzate",
                subtitle = "Pulizia foto orfane, manutenzione ed eliminazione",
                icon = Icons.Default.Build,
                iconTint = MaterialTheme.colorScheme.secondary
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // A. PULIZIA FOTO ORFANE
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Pulizia Foto Orfane",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Scansiona ed elimina le immagini salvate sul telefono che non appartengono a nessuna ricetta attiva.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = { viewModel.cleanupOrphanPhotos(context) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pulisci Foto Non Utilizzate")
                        }
                    }

                    HorizontalDivider()

                    // B. IMPORTAZIONE FILE .RTK (MY RECIPE BOX)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Importa Backup My Recipe Box (.rtk)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Importa ricette ed immagini da un file di archivio .rtk.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        AnimatedVisibility(visible = state.isImportingRtk) {
                            Column {
                                Text("Importazione .rtk in corso...", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }
                        }
                        OutlinedButton(
                            onClick = { rtkFilePickerLauncher.launch(arrayOf("*/*")) },
                            enabled = !state.isImportingRtk,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Seleziona File .rtk")
                        }
                    }

                    HorizontalDivider()

                    // C. MANUTENZIONE STATO RICETTE
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Manutenzione Stato Ricette",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Azzera il flag 'Provata' impostando tutte le ricette allo stato 'Da provare'.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = { showResetTriedDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Azzera stato 'Provata' su tutte le ricette")
                        }
                    }

                    HorizontalDivider()

                    // D. RESET DATABASE PERICOLOSO
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Reset Database",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Elimina permanentemente tutte le ricette locali nel database.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = { showResetDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset Completo Database")
                        }
                    }
                }
            }

            // FOOTER IN CENTRO IN FONDO ALLE IMPOSTAZIONI IDENTICO ALLA HOME
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_icon),
                    contentDescription = "Logo Le Mie Ricette",
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Le Mie Ricette v1.0.0",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * Componente riutilizzabile per le schede collassabili della schermata Impostazioni.
 */
@Composable
private fun CollapsibleSettingsCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    initiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // HEADER CLICCABILE CON FRECCIA ESPANDI/COLLASSA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (!isExpanded && subtitle.isNotBlank()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Riduci" else "Espandi"
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    content()
                }
            }
        }
    }
}
