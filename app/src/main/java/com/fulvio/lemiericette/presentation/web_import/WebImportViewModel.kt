package com.fulvio.lemiericette.presentation.web_import

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fulvio.lemiericette.core.util.NotificationUtils
import com.fulvio.lemiericette.core.util.UrlUtils
import com.fulvio.lemiericette.data.remote.downloader.ImageDownloader
import com.fulvio.lemiericette.data.remote.scraper.RecipeScraper
import com.fulvio.lemiericette.domain.model.Recipe
import com.fulvio.lemiericette.domain.model.ScrapedRecipe
import com.fulvio.lemiericette.domain.model.SearchEngine
import com.fulvio.lemiericette.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

private const val TAG = "WebViewLeak"

data class WebImportState(
    val searchQuery: String = "",
    val directUrlInput: String = "",
    val currentUrl: String = "",
    val activeUrl: String = "",
    val isLoading: Boolean = false,
    val loadingProgress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val searchEngine: SearchEngine = SearchEngine.DUCKDUCKGO,
    val isSearching: Boolean = true,
    val isExtracting: Boolean = false,
    val showPreviewDialog: Boolean = false,
    val scrapedRecipe: ScrapedRecipe? = null,
    val importSuccess: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class WebImportViewModel @Inject constructor(
    private val repository: RecipeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialQuery: String? = savedStateHandle["initialQuery"]
    private val rawSharedUrl: String? = savedStateHandle["sharedUrl"]

    private var webViewRef: WebView? = null

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    private val _state = MutableStateFlow(WebImportState())
    val state: StateFlow<WebImportState> = _state.asStateFlow()

    init {
        val decodedSharedUrl = decodeUrl(rawSharedUrl)
        if (!decodedSharedUrl.isNullOrBlank()) {
            _state.update {
                it.copy(
                    activeUrl = decodedSharedUrl,
                    currentUrl = decodedSharedUrl,
                    isSearching = false
                )
            }
        } else if (!initialQuery.isNullOrBlank()) {
            _state.update { it.copy(searchQuery = initialQuery) }
            startSearch()
        }
    }

    fun registerWebView(webView: WebView) {
        webViewRef = webView
        Log.d(TAG, "WebImportViewModel: WebView registrata con successo")
    }

    fun destroyWebView() {
        Log.d(TAG, "WebImportViewModel: destroyWebView() avviato")
        webViewRef?.let { webView ->
            try {
                webView.stopLoading()
                webView.webViewClient = WebViewClient()
                webView.webChromeClient = null
                (webView.parent as? ViewGroup)?.removeView(webView)
                webView.clearHistory()
                webView.clearCache(true)
                webView.loadUrl("about:blank")
                webView.onPause()
                webView.removeAllViews()
                webView.destroy()
                Log.d(TAG, "WebImportViewModel: WebView distrutta con successo! (Contatore a 0)")
            } catch (e: Exception) {
                Log.e(TAG, "Errore durante la distruzione nel ViewModel: ${e.localizedMessage}", e)
            }
        }
        webViewRef = null
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "WebImportViewModel: onCleared() invocato per lo smontaggio della schermata")
        destroyWebView()
    }

    private fun decodeUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null
        return try {
            URLDecoder.decode(url, StandardCharsets.UTF_8.name())
        } catch (_: Exception) {
            url
        }
    }

    fun initSharedUrlExtraction(context: Context) {
        val decodedSharedUrl = decodeUrl(rawSharedUrl)
        if (!decodedSharedUrl.isNullOrBlank() && _state.value.scrapedRecipe == null && !_state.value.isExtracting && !_state.value.showPreviewDialog) {
            extractRecipeFromSharedUrl(decodedSharedUrl, context)
        }
    }

    fun onQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun onDirectUrlInputChanged(url: String) {
        _state.update { it.copy(directUrlInput = url) }
    }

    fun onSearchEngineChanged(engine: SearchEngine) {
        _state.update { it.copy(searchEngine = engine) }
    }

    fun onUrlInputChanged(url: String) {
        _state.update { it.copy(currentUrl = url) }
    }

    fun startSearch() {
        val query = _state.value.searchQuery
        if (query.isBlank()) return

        val searchUrl = _state.value.searchEngine.buildSearchUrl(query)
        _state.update {
            it.copy(
                activeUrl = searchUrl,
                currentUrl = searchUrl,
                isSearching = false,
                isLoading = true
            )
        }
    }

    fun importDirectUrl(context: Context) {
        val rawInput = _state.value.directUrlInput.trim()
        val extractedUrl = UrlUtils.extractUrlFromText(rawInput)

        if (extractedUrl.isNullOrBlank()) {
            _state.update {
                it.copy(message = "Inserisci un indirizzo URL valido (es. https://...)")
            }
            return
        }

        extractRecipeFromSharedUrl(extractedUrl, context)
    }

    fun loadCustomUrl(url: String) {
        if (url.isBlank()) return
        val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else url

        _state.update {
            it.copy(
                activeUrl = formattedUrl,
                currentUrl = formattedUrl,
                isSearching = false,
                isLoading = true
            )
        }
    }

    fun resetToSearchMode() {
        _state.update { it.copy(isSearching = true) }
    }

    fun onUrlLoaded(url: String) {
        _state.update {
            it.copy(
                currentUrl = url,
                activeUrl = url
            )
        }
    }

    fun onLoadingProgressChanged(progress: Int) {
        _state.update {
            it.copy(
                loadingProgress = progress,
                isLoading = progress < 100
            )
        }
    }

    fun onNavigationStateChanged(canGoBack: Boolean, canGoForward: Boolean) {
        _state.update {
            it.copy(
                canGoBack = canGoBack,
                canGoForward = canGoForward
            )
        }
    }

    fun extractRecipeFromPage(htmlContent: String, context: Context) {
        val targetUrl = _state.value.currentUrl.ifBlank { _state.value.activeUrl }
        _state.update { it.copy(isExtracting = true) }

        viewModelScope.launch(Dispatchers.IO) {
            var extracted = RecipeScraper.parseHtmlToRecipe(htmlContent, targetUrl)

            if (extracted.title.isBlank() || isInvalidTitle(extracted.title)) {
                val candidateTitle = UrlUtils.extractTitleFromSharedText(initialQuery, targetUrl)
                    ?: if (targetUrl.contains("facebook.com", ignoreCase = true)) "Reel di Facebook" else "Ricetta da Web"
                extracted = extracted.copy(title = candidateTitle)
            }

            val localImagePath = ImageDownloader.downloadImageToInternalStorage(context, extracted.imageUrl)
            val finalScraped = extracted.copy(imageUrl = localImagePath ?: extracted.imageUrl)

            _state.update {
                it.copy(
                    isExtracting = false,
                    scrapedRecipe = finalScraped,
                    showPreviewDialog = true
                )
            }
        }
    }

    fun extractRecipeFromSharedUrl(targetUrl: String, context: Context) {
        _state.update { it.copy(isExtracting = true) }

        viewModelScope.launch(Dispatchers.IO) {
            val htmlContent = try {
                val request = Request.Builder()
                    .url(targetUrl)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                    .build()
                val response = okHttpClient.newCall(request).execute()
                response.body?.string() ?: ""
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }

            var extracted = RecipeScraper.parseHtmlToRecipe(htmlContent, targetUrl)

            // Pulizia automatica dei titoli "Error | Facebook" o vuoti con il testo del post condiviso
            if (extracted.title.isBlank() || isInvalidTitle(extracted.title)) {
                val candidateTitle = UrlUtils.extractTitleFromSharedText(initialQuery, targetUrl)
                    ?: if (targetUrl.contains("facebook.com", ignoreCase = true)) "Reel di Facebook" else "Ricetta da Web"
                extracted = extracted.copy(title = candidateTitle)
            }

            val localImagePath = ImageDownloader.downloadImageToInternalStorage(context, extracted.imageUrl)
            val finalScraped = extracted.copy(imageUrl = localImagePath ?: extracted.imageUrl)

            _state.update {
                it.copy(
                    isExtracting = false,
                    scrapedRecipe = finalScraped,
                    showPreviewDialog = true
                )
            }
        }
    }

    private fun isInvalidTitle(title: String): Boolean {
        val lower = title.lowercase().trim()
        return lower.isBlank() ||
                lower.contains("error | facebook") ||
                lower.contains("error facebook") ||
                lower.contains("accedi a facebook") ||
                lower.contains("log in to facebook") ||
                lower == "facebook"
    }

    fun dismissPreviewDialog() {
        _state.update { it.copy(showPreviewDialog = false) }
    }

    fun saveImportedRecipe(context: Context, recipeToSave: Recipe) {
        viewModelScope.launch {
            repository.insertOrUpdateRecipe(recipeToSave)
            NotificationUtils.showImportNotification(context, recipeToSave.title)
            _state.update {
                it.copy(
                    showPreviewDialog = false,
                    importSuccess = true
                )
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
}
