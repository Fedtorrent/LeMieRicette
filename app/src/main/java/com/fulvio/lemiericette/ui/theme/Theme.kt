package com.fulvio.lemiericette.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.fulvio.lemiericette.domain.model.AppFontStyle
import com.fulvio.lemiericette.domain.model.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = TerracottaOnPrimary,
    primaryContainer = TerracottaContainer,
    onPrimaryContainer = TerracottaOnContainer,
    secondary = AmberSecondary,
    onSecondary = AmberOnSecondary,
    secondaryContainer = AmberContainer,
    onSecondaryContainer = AmberOnContainer,
    background = CreamBackground,
    onBackground = DarkBrownText,
    surface = CreamBackground,
    onSurface = DarkBrownText,
    surfaceVariant = WhiteCardSurface,
    onSurfaceVariant = DarkBrownText
)

private val DarkColorScheme = darkColorScheme(
    primary = TerracottaPrimary,
    onPrimary = TerracottaOnPrimary,
    primaryContainer = TerracottaContainerDark,
    onPrimaryContainer = TerracottaOnContainerDark,
    secondary = AmberSecondary,
    onSecondary = AmberOnSecondary,
    secondaryContainer = AmberContainerDark,
    onSecondaryContainer = AmberOnContainerDark,
    background = DarkThemeBackground,
    onBackground = DarkThemeText,
    surface = DarkThemeBackground,
    onSurface = DarkThemeText,
    surfaceVariant = DarkThemeCardSurface,
    onSurfaceVariant = DarkThemeText
)

@Composable
fun LeMieRicetteTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    appFontStyle: AppFontStyle = AppFontStyle.STANDARD,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val typography = getAppTypography(appFontStyle)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
