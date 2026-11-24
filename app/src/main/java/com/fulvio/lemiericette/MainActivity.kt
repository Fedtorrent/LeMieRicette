package com.fulvio.lemiericette

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.fulvio.lemiericette.core.util.UrlUtils
import com.fulvio.lemiericette.data.local.preferences.UserPreferencesRepository
import com.fulvio.lemiericette.domain.model.AppFontStyle
import com.fulvio.lemiericette.domain.model.ThemeMode
import com.fulvio.lemiericette.presentation.navigation.NavGraph
import com.fulvio.lemiericette.presentation.navigation.Screen
import com.fulvio.lemiericette.ui.theme.LeMieRicetteTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    private var sharedUrlState by mutableStateOf<String?>(null)
    private var sharedTextState by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        enableEdgeToEdge()

        setContent {
            val themeMode by userPreferencesRepository.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            val appFontStyle by userPreferencesRepository.appFontStyle.collectAsStateWithLifecycle(initialValue = AppFontStyle.HANDWRITTEN)
            val keepScreenOn by userPreferencesRepository.keepScreenOn.collectAsStateWithLifecycle(initialValue = false)

            // Gestione del flag di sistema per lo schermo sempre acceso
            LaunchedEffect(keepScreenOn) {
                if (keepScreenOn) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            LeMieRicetteTheme(themeMode = themeMode, appFontStyle = appFontStyle) {
                val navController = rememberNavController()

                NavGraph(
                    navController = navController,
                    startDestination = Screen.CategoryList.route
                )

                LaunchedEffect(sharedUrlState) {
                    val urlToImport = sharedUrlState
                    if (!urlToImport.isNullOrBlank()) {
                        val textToPass = sharedTextState
                        sharedUrlState = null
                        sharedTextState = null
                        navController.navigate(Screen.WebImport.createRoute(sharedUrl = urlToImport, initialQuery = textToPass))
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            val extractedUrl = UrlUtils.extractUrlFromText(sharedText)

            // Consumo immediato dell'Intent per prevenire ri-elaborazioni impreviste
            intent.action = Intent.ACTION_MAIN
            intent.removeExtra(Intent.EXTRA_TEXT)

            if (!extractedUrl.isNullOrBlank()) {
                sharedUrlState = extractedUrl
                sharedTextState = sharedText
            }
        }
    }
}
