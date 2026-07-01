package tv.darshini.app.ui.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColorPalette(
    val canvas: Color,
    val canvasElevated: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceEmphasis: Color,
    val surfaceAccent: Color,

    val brand: Color,
    val brandMuted: Color,
    val brandStrong: Color,
    val focus: Color,

    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textDisabled: Color,

    val live: Color,
    val success: Color,
    val warning: Color,
    val info: Color,

    val divider: Color,
    val outline: Color,

    val heroTop: Color,
    val heroBottom: Color
)

val DarkAppColors = AppColorPalette(
    canvas = Color(0xFF07111B),
    canvasElevated = Color(0xFF0B1622),
    surface = Color(0xFF0F1B29),
    surfaceElevated = Color(0xFF162338),
    surfaceEmphasis = Color(0xFF1D2E46),
    surfaceAccent = Color(0xFF223754),

    brand = Color(0xFF69A8FF),
    brandMuted = Color(0x335FA4FF),
    brandStrong = Color(0xFF8BBCFF),
    focus = Color(0xFFF4F8FF),

    textPrimary = Color(0xFFF5F7FB),
    textSecondary = Color(0xFFBBC6D8),
    textTertiary = Color(0xFF7F8DA5),
    textDisabled = Color(0xFF566173),

    live = Color(0xFFFF5C61),
    success = Color(0xFF4FD39A),
    warning = Color(0xFFFFC766),
    info = Color(0xFF57C9FF),

    divider = Color(0x1AF4F8FF),
    outline = Color(0x264C6D95),

    heroTop = Color(0xCC07111B),
    heroBottom = Color(0xF207111B)
)

val LightAppColors = AppColorPalette(
    canvas = Color(0xFFF5F7FB),
    canvasElevated = Color(0xFFEBF0F6),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFE1E8F0),
    surfaceEmphasis = Color(0xFFD4DEEB),
    surfaceAccent = Color(0xFFC7D4E5),

    brand = Color(0xFF1A56B0),
    brandMuted = Color(0x221A56B0),
    brandStrong = Color(0xFF0D3D82),
    focus = Color(0xFF1C2733),

    textPrimary = Color(0xFF1C2733),
    textSecondary = Color(0xFF4C5E75),
    textTertiary = Color(0xFF7086A0),
    textDisabled = Color(0xFF9FB2C8),

    live = Color(0xFFD32F2F),
    success = Color(0xFF2E7D32),
    warning = Color(0xFFF57C00),
    info = Color(0xFF0288D1),

    divider = Color(0x1A1C2733),
    outline = Color(0x264C5E75),

    heroTop = Color(0xCCEBF0F6),
    heroBottom = Color(0xF2EBF0F6)
)

val GlassDarkAppColors = AppColorPalette(
    canvas = Color(0xFF070F18),
    canvasElevated = Color(0xFF0A1420),
    surface = Color(0x4D0E1C2E), // Translucent frosted surface
    surfaceElevated = Color(0x66182A42),
    surfaceEmphasis = Color(0x80223857),
    surfaceAccent = Color(0x992D476C),

    brand = Color(0xFF8BBCFF),
    brandMuted = Color(0x2B8BBCFF),
    brandStrong = Color(0xFFB5D4FF),
    focus = Color(0xFFFFFFFF),

    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFCCD7E6),
    textTertiary = Color(0xFF8FA1BA),
    textDisabled = Color(0xFF5E6F85),

    live = Color(0xFFFF6E73),
    success = Color(0xFF5FF3B6),
    warning = Color(0xFFFFD485),
    info = Color(0xFF75D5FF),

    divider = Color(0x1CFFFFFF),
    outline = Color(0x338BBCFF),

    heroTop = Color(0x80070F18),
    heroBottom = Color(0xE6070F18)
)

val GlassLightAppColors = AppColorPalette(
    canvas = Color(0xFFF0F3F8),
    canvasElevated = Color(0xFFE2E7F0),
    surface = Color(0x59FFFFFF), // Frosted glass light
    surfaceElevated = Color(0x73FFFFFF),
    surfaceEmphasis = Color(0x8CFFFFFF),
    surfaceAccent = Color(0xA6FFFFFF),

    brand = Color(0xFF1A56B0),
    brandMuted = Color(0x221A56B0),
    brandStrong = Color(0xFF0D3D82),
    focus = Color(0xFF1C2733),

    textPrimary = Color(0xFF1C2733),
    textSecondary = Color(0xFF4C5E75),
    textTertiary = Color(0xFF7086A0),
    textDisabled = Color(0xFF9FB2C8),

    live = Color(0xFFD32F2F),
    success = Color(0xFF2E7D32),
    warning = Color(0xFFF57C00),
    info = Color(0xFF0288D1),

    divider = Color(0x1F1C2733),
    outline = Color(0x331A56B0),

    heroTop = Color(0x80E2E7F0),
    heroBottom = Color(0xE6E2E7F0)
)

val LocalAppColors = staticCompositionLocalOf { GlassDarkAppColors }

object AppColors {
    val Canvas: Color @Composable get() = LocalAppColors.current.canvas
    val CanvasElevated: Color @Composable get() = LocalAppColors.current.canvasElevated
    val Surface: Color @Composable get() = LocalAppColors.current.surface
    val SurfaceElevated: Color @Composable get() = LocalAppColors.current.surfaceElevated
    val SurfaceEmphasis: Color @Composable get() = LocalAppColors.current.surfaceEmphasis
    val SurfaceAccent: Color @Composable get() = LocalAppColors.current.surfaceAccent

    val Brand: Color @Composable get() = LocalAppColors.current.brand
    val BrandMuted: Color @Composable get() = LocalAppColors.current.brandMuted
    val BrandStrong: Color @Composable get() = LocalAppColors.current.brandStrong
    val Focus: Color @Composable get() = LocalAppColors.current.focus

    val TextPrimary: Color @Composable get() = LocalAppColors.current.textPrimary
    val TextSecondary: Color @Composable get() = LocalAppColors.current.textSecondary
    val TextTertiary: Color @Composable get() = LocalAppColors.current.textTertiary
    val TextDisabled: Color @Composable get() = LocalAppColors.current.textDisabled

    val Live: Color @Composable get() = LocalAppColors.current.live
    val Success: Color @Composable get() = LocalAppColors.current.success
    val Warning: Color @Composable get() = LocalAppColors.current.warning
    val Info: Color @Composable get() = LocalAppColors.current.info

    val Divider: Color @Composable get() = LocalAppColors.current.divider
    val Outline: Color @Composable get() = LocalAppColors.current.outline

    val HeroTop: Color @Composable get() = LocalAppColors.current.heroTop
    val HeroBottom: Color @Composable get() = LocalAppColors.current.heroBottom
}
