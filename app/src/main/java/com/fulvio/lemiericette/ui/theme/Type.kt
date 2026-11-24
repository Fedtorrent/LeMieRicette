package com.fulvio.lemiericette.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.fulvio.lemiericette.domain.model.AppFontStyle

fun getAppTypography(appFontStyle: AppFontStyle): Typography {
    val fontFamily = when (appFontStyle) {
        AppFontStyle.STANDARD -> FontFamily.Default
        AppFontStyle.HANDWRITTEN -> FontFamily.Cursive
    }

    val sizeMultiplier = if (appFontStyle == AppFontStyle.HANDWRITTEN) 1.32f else 1.0f

    return Typography(
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (32.sp.value * sizeMultiplier).sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp
        ),
        displayMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (28.sp.value * sizeMultiplier).sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (24.sp.value * sizeMultiplier).sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (28.sp.value * sizeMultiplier).sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (24.sp.value * sizeMultiplier).sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (20.sp.value * sizeMultiplier).sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (20.sp.value * sizeMultiplier).sp,
            lineHeight = 26.sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (16.sp.value * sizeMultiplier).sp,
            lineHeight = 22.sp,
            letterSpacing = 0.15.sp
        ),
        titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (14.sp.value * sizeMultiplier).sp,
            lineHeight = 18.sp,
            letterSpacing = 0.1.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (16.sp.value * sizeMultiplier).sp,
            lineHeight = 22.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (14.sp.value * sizeMultiplier).sp,
            lineHeight = 18.sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (12.sp.value * sizeMultiplier).sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        ),
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (14.sp.value * sizeMultiplier).sp,
            lineHeight = 18.sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (12.sp.value * sizeMultiplier).sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (11.sp.value * sizeMultiplier).sp,
            lineHeight = 14.sp,
            letterSpacing = 0.5.sp
        )
    )
}

val Typography = getAppTypography(AppFontStyle.HANDWRITTEN)
