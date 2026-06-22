package com.streamvault.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme
import com.streamvault.app.ui.design.AppColors
import com.streamvault.app.ui.design.AppShapes
import com.streamvault.app.ui.design.LocalAppShapes
import com.streamvault.app.ui.design.LocalAppSpacing
import com.streamvault.app.ui.design.rememberAppTypography

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.lightColorScheme
import com.streamvault.app.ui.design.LocalAppColors
import com.streamvault.app.ui.design.DarkAppColors
import com.streamvault.app.ui.design.LightAppColors
import com.streamvault.app.ui.design.GlassDarkAppColors
import com.streamvault.app.ui.design.GlassLightAppColors

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1A56B0),
    onPrimary = OnPrimary,
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C2733),
    surfaceVariant = Color(0xFFE1E8F0),
    onSurfaceVariant = Color(0xFF4C5E75),
    background = Color(0xFFEBF0F6),
    onBackground = Color(0xFF1C2733),
    error = Color(0xFFD32F2F),
    onError = OnPrimary
)

@Composable
fun StreamVaultTheme(
    appTheme: String = "glass_dark",
    content: @Composable () -> Unit
) {
    val selectedPalette = when (appTheme) {
        "light" -> LightAppColors
        "dark" -> DarkAppColors
        "glass_dark" -> GlassDarkAppColors
        "glass_light" -> GlassLightAppColors
        else -> if (isSystemInDarkTheme()) DarkAppColors else LightAppColors
    }
    val colorScheme = if (selectedPalette == LightAppColors || selectedPalette == GlassLightAppColors) {
        lightColorScheme(
            primary = selectedPalette.brand,
            onPrimary = OnPrimary,
            surface = selectedPalette.surface,
            onSurface = selectedPalette.textPrimary,
            surfaceVariant = selectedPalette.surfaceElevated,
            onSurfaceVariant = selectedPalette.textSecondary,
            background = selectedPalette.canvasElevated,
            onBackground = selectedPalette.textPrimary,
            error = selectedPalette.live,
            onError = OnPrimary,
            inverseOnSurface = selectedPalette.textPrimary
        )
    } else {
        // Dark or Glass themes use darkColorScheme
        darkColorScheme(
            primary = selectedPalette.brand,
            onPrimary = OnPrimary,
            surface = selectedPalette.surface,
            onSurface = selectedPalette.textPrimary,
            surfaceVariant = selectedPalette.surfaceElevated,
            onSurfaceVariant = selectedPalette.textSecondary,
            background = selectedPalette.canvasElevated,
            onBackground = selectedPalette.textPrimary,
            error = selectedPalette.live,
            onError = OnPrimary,
            inverseOnSurface = selectedPalette.textPrimary
        )
    }
    val typography = rememberAppTypography()

    CompositionLocalProvider(
        LocalAppSpacing provides com.streamvault.app.ui.design.AppSpacing(),
        LocalAppShapes provides AppShapes(),
        LocalAppColors provides selectedPalette
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
