package com.streamvault.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.streamvault.app.ui.design.AppColors

val Primary: Color
    @Composable get() = AppColors.Brand

val PrimaryLight: Color
    @Composable get() = AppColors.BrandStrong

val PrimaryGlow: Color
    @Composable get() = AppColors.BrandMuted

val BackgroundDeep: Color
    @Composable get() = AppColors.Canvas

val Background: Color
    @Composable get() = AppColors.CanvasElevated

val Surface: Color
    @Composable get() = AppColors.Surface

val SurfaceElevated: Color
    @Composable get() = AppColors.SurfaceElevated

val SurfaceHighlight: Color
    @Composable get() = AppColors.SurfaceEmphasis

val TextPrimary: Color
    @Composable get() = AppColors.TextPrimary

val TextSecondary: Color
    @Composable get() = AppColors.TextSecondary

val TextTertiary: Color
    @Composable get() = AppColors.TextTertiary

val TextDisabled: Color
    @Composable get() = AppColors.TextDisabled

val OnBackground: Color
    @Composable get() = TextPrimary

val OnSurface: Color
    @Composable get() = TextPrimary

val OnSurfaceDim: Color
    @Composable get() = TextTertiary

val AccentRed: Color
    @Composable get() = AppColors.Live

val AccentGreen: Color
    @Composable get() = AppColors.Success

val AccentAmber: Color
    @Composable get() = AppColors.Warning

val AccentCyan: Color
    @Composable get() = AppColors.Info

val OnPrimary = Color(0xFFFFFFFF)

val Secondary: Color
    @Composable get() = AppColors.Success

val ErrorColor: Color
    @Composable get() = AccentRed

val GradientOverlayTop: Color
    @Composable get() = AppColors.HeroTop

val GradientOverlayBottom: Color
    @Composable get() = AppColors.HeroBottom

val FocusBorder: Color
    @Composable get() = AppColors.Focus

val ProgressBarBackground: Color
    @Composable get() = AppColors.SurfaceAccent

val SettingsCardBackground: Color
    @Composable get() {
        val currentPalette = com.streamvault.app.ui.design.LocalAppColors.current
        return when (currentPalette) {
            com.streamvault.app.ui.design.DarkAppColors -> Color.White.copy(alpha = 0.035f)
            com.streamvault.app.ui.design.GlassDarkAppColors -> Color.White.copy(alpha = 0.035f)
            com.streamvault.app.ui.design.GlassLightAppColors -> Color.White.copy(alpha = 0.30f)
            else -> Color.White
        }
    }

val DialogBackground: Color
    @Composable get() {
        val currentPalette = com.streamvault.app.ui.design.LocalAppColors.current
        val isGlass = currentPalette == com.streamvault.app.ui.design.GlassLightAppColors || currentPalette == com.streamvault.app.ui.design.GlassDarkAppColors
        return if (isGlass) {
            currentPalette.canvasElevated.copy(alpha = 0.99f)
        } else {
            currentPalette.canvasElevated
        }
    }

