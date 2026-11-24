package com.fulvio.lemiericette.presentation.web_import

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fulvio.lemiericette.core.ui.components.AppTopBar
import com.fulvio.lemiericette.core.ui.components.LoadingScreen
import com.fulvio.lemiericette.core.util.WebSessionManager
import com.fulvio.lemiericette.domain.model.SearchEngine
import com.fulvio.lemiericette.presentation.web_import.components.ImportPreviewDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "WebViewLeak"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebImportScreen(
    onBackClick: () -> Unit,
    viewModel: WebImportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var activeWebView: WebView? = remember { null }

    // GESTIONE DEL CICLO DI VITA E PREVENZIONE MEMORY LEAK VIA DISPOSABLE EFFECT E VIEWMODEL
    DisposableEffect(Unit) {
        Log.d(TAG, "WebImportScreen: Composable montata")
        onDispose {
            Log.d(TAG, "WebImportScreen: Composable smontata -> Invocazione di viewModel.destroyWebView()")
            viewModel.destroyWebView()
        }
    }

    // Avvia l'estrazione automatica se la schermata è stata aperta tramite Intent di Condivisione
    LaunchedEffect(Unit) {
        viewModel.initSharedUrlExtraction(context)
    }

    LaunchedEffect(state.importSuccess) {
        if (state.importSuccess) {
            Toast.makeText(context, "Ricetta importata con successo!", Toast.LENGTH_SHORT).show()
            onBackClick()
        }
    }

    LaunchedEffect(state.message) {
        state.message?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    // Gestione del tasto indietro di sistema (Gesture / Pulsante fisico Android)
    BackHandler {
        when {
            state.showPreviewDialog -> viewModel.dismissPreviewDialog()
            activeWebView?.canGoBack() == true -> activeWebView?.goBack()
            !state.isSearching -> viewModel.resetToSearchMode()
            else -> onBackClick()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (state.isSearching) "Cerca & Importa Ricetta" else "Browser Interno",
                onBackClick = {
                    when {
                        state.showPreviewDialog -> viewModel.dismissPreviewDialog()
                        activeWebView?.canGoBack() == true -> activeWebView?.goBack()
                        !state.isSearching -> viewModel.resetToSearchMode()
                        else -> onBackClick()
                    }
                },
                actions = {
                    if (!state.isSearching) {
                        IconButton(onClick = { viewModel.resetToSearchMode() }) {
                            Icon(Icons.Default.Search, contentDescription = "Nuova ricerca")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!state.isSearching && !state.isExtracting && !state.showPreviewDialog) {
                ExtendedFloatingActionButton(
                    onClick = {
                        activeWebView?.evaluateJavascript(
                            "(function() { return document.documentElement.outerHTML; })();"
                        ) { htmlJson ->
                            if (!htmlJson.isNullOrBlank()) {
                                val cleanHtml = try {
                                    org.json.JSONTokener(htmlJson).nextValue().toString()
                                } catch (_: Exception) {
                                    htmlJson
                                }
                                viewModel.extractRecipeFromPage(cleanHtml, context)
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Download, contentDescription = null) },
                    text = { Text("Importa Ricetta") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isSearching) {
                // SCHERMATA INIZIALE DI RICERCA ED IMPORTAZIONE DA LINK DIRETTO
                SearchPromptSection(
                    searchQuery = state.searchQuery,
                    directUrlInput = state.directUrlInput,
                    selectedEngine = state.searchEngine,
                    onQueryChanged = { viewModel.onQueryChanged(it) },
                    onDirectUrlChanged = { viewModel.onDirectUrlInputChanged(it) },
                    onEngineSelected = { viewModel.onSearchEngineChanged(it) },
                    onSearchClick = { viewModel.startSearch() },
                    onImportDirectUrlClick = { viewModel.importDirectUrl(context) }
                )
            } else {
                // BROWSER INTERNO WEBVIEW CON BARRA DI NAVIGAZIONE E SICUREZZA
                Column(modifier = Modifier.fillMaxSize()) {

                    // BARRA NAVIGAZIONE BROWSER
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { activeWebView?.goBack() },
                                    enabled = state.canGoBack
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Indietro"
                                    )
                                }

                                IconButton(
                                    onClick = { activeWebView?.goForward() },
                                    enabled = state.canGoForward
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Avanti"
                                    )
                                }

                                IconButton(
                                    onClick = { activeWebView?.reload() }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Ricarica"
                                    )
                                }

                                OutlinedTextField(
                                    value = state.currentUrl,
                                    onValueChange = { viewModel.onUrlInputChanged(it) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                                    keyboardActions = KeyboardActions(onGo = {
                                        viewModel.loadCustomUrl(state.currentUrl)
                                    }),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 4.dp),
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                            }

                            AnimatedVisibility(visible = state.isLoading) {
                                LinearProgressIndicator(
                                    progress = { state.loadingProgress / 100f },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // CONTAINER WEBVIEW CONFIGURATO CON REGOLE DI SICUREZZA, MEMORIA ED ONRELEASE CALLBACK
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                activeWebView = this
                                viewModel.registerWebView(this)
                                WebSessionManager.configureWebViewSettings(this)

                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): Boolean {
                                        val url = request?.url?.toString() ?: return false
                                        return if (url.startsWith("http://") || url.startsWith("https://")) {
                                            false
                                        } else {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                ctx.startActivity(intent)
                                            } catch (_: Exception) {}
                                            true
                                        }
                                    }

                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        super.onPageStarted(view, url, favicon)
                                        url?.let { viewModel.onUrlLoaded(it) }
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        url?.let { viewModel.onUrlLoaded(it) }
                                        viewModel.onNavigationStateChanged(
                                            canGoBack = view?.canGoBack() ?: false,
                                            canGoForward = view?.canGoForward() ?: false
                                        )
                                    }
                                }

                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        super.onProgressChanged(view, newProgress)
                                        viewModel.onLoadingProgressChanged(newProgress)
                                    }
                                }
                            }
                        },
                        update = { webView ->
                            activeWebView = webView
                            viewModel.registerWebView(webView)
                            if (state.activeUrl.isNotBlank() && webView.url != state.activeUrl) {
                                webView.loadUrl(state.activeUrl)
                            }
                        },
                        onRelease = {
                            Log.d(TAG, "AndroidView onRelease: Chiamata a viewModel.destroyWebView()")
                            viewModel.destroyWebView()
                            activeWebView = null
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // LOADING DIALOG PER ESTRAZIONE IN CORSO
            if (state.isExtracting) {
                LoadingScreen(message = "Analisi pagina ed estrazione ricetta in corso...")
            }

            // ANTEPRIMA MODIFICABILE RICETTA
            if (state.showPreviewDialog && state.scrapedRecipe != null) {
                ImportPreviewDialog(
                    scrapedRecipe = state.scrapedRecipe!!,
                    onDismiss = { viewModel.dismissPreviewDialog() },
                    onSaveRecipe = { recipeToSave ->
                        viewModel.saveImportedRecipe(context, recipeToSave)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun SearchPromptSection(
    searchQuery: String,
    directUrlInput: String,
    selectedEngine: SearchEngine,
    onQueryChanged: (String) -> Unit,
    onDirectUrlChanged: (String) -> Unit,
    onEngineSelected: (SearchEngine) -> Unit,
    onSearchClick: () -> Unit,
    onImportDirectUrlClick: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    val isImeVisible = WindowInsets.isImeVisible
    val coroutineScope = rememberCoroutineScope()

    val searchFieldBringIntoView = remember { BringIntoViewRequester() }
    val directUrlBringIntoView = remember { BringIntoViewRequester() }

    // Ritorno in cima in automatico quando la tastiera viene chiusa
    LaunchedEffect(isImeVisible) {
        if (!isImeVisible) {
            scrollState.animateScrollTo(0)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // SCHEDA 1: RICERCA STANDARD SUI MOTORI
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(searchFieldBringIntoView),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Ricerca Ricette sul Web",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // CAMPO DI RICERCA 1 (IN ALTO)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onQueryChanged,
                    label = { Text("Nome della ricetta") },
                    placeholder = { Text("Es. Lasagne alla bolognese") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearchClick() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                coroutineScope.launch {
                                    delay(200)
                                    searchFieldBringIntoView.bringIntoView()
                                }
                            }
                        }
                )

                // SELETTORE MOTORE DI RICERCA
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Motore di Ricerca:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SearchEngine.entries.forEach { engine ->
                            FilterChip(
                                selected = selectedEngine == engine,
                                onClick = { onEngineSelected(engine) },
                                label = { Text(engine.displayName) }
                            )
                        }
                    }
                }

                Button(
                    onClick = onSearchClick,
                    enabled = searchQuery.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerca Ricetta sul Web")
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // SCHEDA 2: IMPORTAZIONE DIRETTA DA LINK (SOCIAL & REELS)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(directUrlBringIntoView),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Importazione da Link (Social & Reel)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Incolla l'indirizzo web di una ricetta o il link di un Reel/Video per avviarne l'importazione automatica.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // CAMPO DI INPUT LINK DIRETTO 2 (IN BASSO)
                OutlinedTextField(
                    value = directUrlInput,
                    onValueChange = onDirectUrlChanged,
                    label = { Text("Link URL o Reel") },
                    placeholder = { Text("https://www.facebook.com/reel/...") },
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                    trailingIcon = {
                        if (directUrlInput.isNotEmpty()) {
                            IconButton(onClick = { onDirectUrlChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Cancella")
                            }
                        } else {
                            IconButton(onClick = {
                                val clipText = clipboardManager.getText()?.text
                                if (!clipText.isNullOrBlank()) {
                                    onDirectUrlChanged(clipText)
                                }
                            }) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Incolla")
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { onImportDirectUrlClick() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                coroutineScope.launch {
                                    delay(200)
                                    directUrlBringIntoView.bringIntoView()
                                }
                            }
                        }
                )

                // PULSANTE IMPORTA LINK DIRETTO
                OutlinedButton(
                    onClick = onImportDirectUrlClick,
                    enabled = directUrlInput.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Importa da Link")
                }

                // ETICHETTA DI AIUTO / INFORMAZIONE PER I SOCIAL
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Per importare da Facebook, Instagram o TikTok: tocca 'Condividi' sul post, seleziona 'Copia Link' e incolla il link qui sopra.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
