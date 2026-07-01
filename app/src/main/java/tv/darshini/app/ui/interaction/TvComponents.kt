package tv.darshini.app.ui.interaction

import android.view.KeyEvent
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonBorder
import androidx.tv.material3.ButtonColors
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ButtonGlow
import androidx.tv.material3.ButtonScale
import androidx.tv.material3.ButtonShape
import androidx.tv.material3.ClickableSurfaceBorder
import androidx.tv.material3.ClickableSurfaceColors
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ClickableSurfaceGlow
import androidx.tv.material3.ClickableSurfaceScale
import androidx.tv.material3.ClickableSurfaceShape
import androidx.tv.material3.IconButton
import androidx.tv.material3.IconButtonDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.MaterialTheme
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import tv.darshini.app.ui.design.LocalAppColors
import tv.darshini.app.ui.design.AppColors
import androidx.compose.foundation.layout.padding

/**
 * Drop-in replacement for TV Material3 Surface(onClick) that automatically adds
 * [mouseClickable] to the modifier so the first finger-tap fires onClick on phones/tablets,
 * while D-pad and mouse navigation on TV remain unchanged.
 */
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@Composable
fun TvClickableSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cornerRadius: androidx.compose.ui.unit.Dp = 12.dp,
    shape: ClickableSurfaceShape = ClickableSurfaceDefaults.shape(androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)),
    colors: ClickableSurfaceColors? = null,
    border: ClickableSurfaceBorder? = null,
    scale: ClickableSurfaceScale = ClickableSurfaceDefaults.scale(),
    glow: ClickableSurfaceGlow = ClickableSurfaceDefaults.glow(),
    interactionSource: MutableInteractionSource? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }
    val colorsPalette = LocalAppColors.current
    val isLightGlass = colorsPalette == tv.darshini.app.ui.design.GlassLightAppColors
    val isGlass = colorsPalette == tv.darshini.app.ui.design.GlassLightAppColors || colorsPalette == tv.darshini.app.ui.design.GlassDarkAppColors

    val resolvedColors = if (colors != null) {
        if (isGlass) {
            val defaultContentColor = colors.contentColor == Color.Unspecified || 
                    colors.contentColor == MaterialTheme.colorScheme.onSurfaceVariant || 
                    colors.contentColor == MaterialTheme.colorScheme.onSurface
            val defaultFocusedContentColor = colors.focusedContentColor == Color.Unspecified || 
                    colors.focusedContentColor == MaterialTheme.colorScheme.inverseOnSurface || 
                    colors.focusedContentColor == MaterialTheme.colorScheme.onSurface

            ClickableSurfaceDefaults.colors(
                containerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                pressedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                contentColor = if (defaultContentColor) colorsPalette.textSecondary else colors.contentColor,
                focusedContentColor = if (defaultFocusedContentColor) colorsPalette.textPrimary else colors.focusedContentColor,
                pressedContentColor = if (defaultFocusedContentColor) colorsPalette.textPrimary else colors.pressedContentColor,
                disabledContentColor = colors.disabledContentColor
            )
        } else {
            colors
        }
    } else {
        if (isGlass) {
            ClickableSurfaceDefaults.colors(
                containerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                pressedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                contentColor = colorsPalette.textSecondary,
                focusedContentColor = colorsPalette.textPrimary,
                pressedContentColor = colorsPalette.textPrimary,
                disabledContentColor = colorsPalette.textDisabled
            )
        } else {
            ClickableSurfaceDefaults.colors(
                containerColor = colorsPalette.surface,
                focusedContainerColor = colorsPalette.surfaceAccent,
                pressedContainerColor = colorsPalette.surfaceEmphasis,
                disabledContainerColor = colorsPalette.surface.copy(alpha = 0.5f),
                contentColor = colorsPalette.textSecondary,
                focusedContentColor = colorsPalette.textPrimary,
                pressedContentColor = colorsPalette.textPrimary,
                disabledContentColor = colorsPalette.textDisabled
            )
        }
    }

    val resolvedBorder = if (isGlass) {
        ClickableSurfaceDefaults.border(
            border = Border.None,
            focusedBorder = Border.None,
            pressedBorder = Border.None,
            disabledBorder = Border.None
        )
    } else {
        border ?: ClickableSurfaceDefaults.border(
            border = Border(
                border = androidx.compose.foundation.BorderStroke(0.dp, Color.Transparent),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)
            ),
            focusedBorder = Border(
                border = androidx.compose.foundation.BorderStroke(tv.darshini.app.ui.design.FocusSpec.BorderWidth, colorsPalette.focus),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)
            ),
            pressedBorder = Border(
                border = androidx.compose.foundation.BorderStroke(tv.darshini.app.ui.design.FocusSpec.BorderWidth, colorsPalette.focus),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)
            )
        )
    }

    Surface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .then(
                if (onLongClick != null) Modifier
                else Modifier.activateOnRemoteKey(enabled = enabled, onClick = onClick)
            )
            .mouseClickable(onClick = onClick, enabled = enabled, onLongClick = onLongClick)
            .drawWithContent {
                if (isGlass) {
                    val currentCorner = cornerRadius.toPx()
                    drawRoundRect(
                        color = if (isFocused) {
                            if (isLightGlass) Color.White.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.12f)
                        } else {
                            if (isLightGlass) Color.White.copy(alpha = 0.30f) else Color.White.copy(alpha = 0.035f)
                        },
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(currentCorner, currentCorner)
                    )

                    if (isFocused && enabled) {
                        // 1. Specular top-down glossy reflection curve
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = if (isLightGlass) {
                                    listOf(
                                        Color.White.copy(alpha = 0.70f),
                                        Color.White.copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                } else {
                                    listOf(
                                        Color.White.copy(alpha = 0.50f),
                                        Color.White.copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                },
                                startY = 0f,
                                endY = size.height * 0.40f
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(currentCorner, currentCorner)
                        )

                        // 2. High-intensity Top-Left Specular highlight (radial shine spotlight catch)
                        val spotlightWidth = 2.5.dp.toPx()
                        val halfSpotlight = spotlightWidth / 2f
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = if (isLightGlass) {
                                    listOf(
                                        Color.White.copy(alpha = 0.90f),
                                        Color.White.copy(alpha = 0.35f),
                                        Color.Transparent
                                    )
                                } else {
                                    listOf(
                                        Color.White.copy(alpha = 0.80f),
                                        Color.White.copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                },
                                center = androidx.compose.ui.geometry.Offset(size.width * 0.15f, 2f),
                                radius = size.width * 0.50f
                            ),
                            topLeft = androidx.compose.ui.geometry.Offset(halfSpotlight, halfSpotlight),
                            size = androidx.compose.ui.geometry.Size(size.width - spotlightWidth, size.height - spotlightWidth),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(currentCorner - halfSpotlight, currentCorner - halfSpotlight),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = spotlightWidth)
                        )

                        // 3. Volumetric double-edge refraction lighting (stacked strokes)
                        // Outer fine edge light
                        val edgeWidth = 1.dp.toPx()
                        val halfEdge = edgeWidth / 2f
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = if (isLightGlass) {
                                    listOf(
                                        Color.White.copy(alpha = 0.60f),
                                        Color.Black.copy(alpha = 0.15f)
                                    )
                                } else {
                                    listOf(
                                        Color.White.copy(alpha = 0.45f),
                                        Color.White.copy(alpha = 0.10f),
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.20f)
                                    )
                                }
                            ),
                            topLeft = androidx.compose.ui.geometry.Offset(halfEdge, halfEdge),
                            size = androidx.compose.ui.geometry.Size(size.width - edgeWidth, size.height - edgeWidth),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(currentCorner - halfEdge, currentCorner - halfEdge),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = edgeWidth)
                        )
                    }
                }

                drawContent()
            },
        enabled = enabled,
        shape = shape,
        colors = resolvedColors,
        border = resolvedBorder,
        scale = scale,
        glow = glow,
        interactionSource = interactionSource,
        content = content,
    )
}

/**
 * Drop-in replacement for TV Material3 Button(onClick) that automatically adds
 * [mouseClickable] so the first finger-tap fires onClick on phones/tablets.
 */
@Composable
fun TvButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    scale: ButtonScale = ButtonDefaults.scale(),
    glow: ButtonGlow = ButtonDefaults.glow(),
    interactionSource: MutableInteractionSource? = null,
    shape: ButtonShape = ButtonDefaults.shape(),
    colors: ButtonColors = ButtonDefaults.colors(
        focusedContainerColor = AppColors.Focus,
        focusedContentColor = AppColors.Canvas
    ),
    border: ButtonBorder = ButtonDefaults.border(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    val colorsPalette = LocalAppColors.current
    val isGlass = colorsPalette == tv.darshini.app.ui.design.GlassLightAppColors || colorsPalette == tv.darshini.app.ui.design.GlassDarkAppColors

    if (isGlass) {
        val focusRequester = remember { FocusRequester() }
        val resolvedColors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            pressedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            contentColor = colorsPalette.textSecondary,
            focusedContentColor = colorsPalette.textPrimary,
            pressedContentColor = colorsPalette.textPrimary,
            disabledContentColor = colorsPalette.textDisabled
        )
        TvClickableSurface(
            onClick = onClick,
            modifier = modifier
                .focusRequester(focusRequester)
                .mouseClickable(onClick = onClick, enabled = enabled),
            enabled = enabled,
            cornerRadius = 20.dp,
            colors = resolvedColors,
            content = {
                Row(
                    modifier = Modifier.padding(contentPadding),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    content = content
                )
            }
        )
    } else {
        Button(
            onClick = onClick,
            modifier = modifier
                .activateOnRemoteKey(enabled = enabled, onClick = onClick)
                .mouseClickable(onClick = onClick, enabled = enabled),
            enabled = enabled,
            scale = scale,
            glow = glow,
            interactionSource = interactionSource,
            shape = shape,
            colors = colors,
            border = border,
            contentPadding = contentPadding,
            content = content,
        )
    }
}

/**
 * Drop-in replacement for TV Material3 IconButton(onClick) that automatically adds
 * [mouseClickable] so the first finger-tap fires onClick on phones/tablets.
 */
@Composable
fun TvIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    scale: ButtonScale = IconButtonDefaults.scale(),
    glow: ButtonGlow = IconButtonDefaults.glow(),
    interactionSource: MutableInteractionSource? = null,
    shape: ButtonShape = IconButtonDefaults.shape(),
    colors: ButtonColors = IconButtonDefaults.colors(
        focusedContainerColor = AppColors.Focus,
        focusedContentColor = AppColors.Canvas
    ),
    border: ButtonBorder = IconButtonDefaults.border(),
    content: @Composable BoxScope.() -> Unit,
) {
    val colorsPalette = LocalAppColors.current
    val isGlass = colorsPalette == tv.darshini.app.ui.design.GlassLightAppColors || colorsPalette == tv.darshini.app.ui.design.GlassDarkAppColors

    if (isGlass) {
        val focusRequester = remember { FocusRequester() }
        val resolvedColors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            pressedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            contentColor = colorsPalette.textSecondary,
            focusedContentColor = colorsPalette.textPrimary,
            pressedContentColor = colorsPalette.textPrimary,
            disabledContentColor = colorsPalette.textDisabled
        )
        TvClickableSurface(
            onClick = onClick,
            onLongClick = onLongClick,
            modifier = modifier
                .focusRequester(focusRequester)
                .mouseClickable(onClick = onClick, enabled = enabled, onLongClick = onLongClick),
            enabled = enabled,
            cornerRadius = 28.dp,
            colors = resolvedColors,
            content = content
        )
    } else {
        IconButton(
            onClick = onClick,
            onLongClick = onLongClick,
            modifier = modifier
                .then(
                    if (onLongClick != null) Modifier
                    else Modifier.activateOnRemoteKey(enabled = enabled, onClick = onClick)
                )
                .mouseClickable(onClick = onClick, enabled = enabled),
            enabled = enabled,
            scale = scale,
            glow = glow,
            interactionSource = interactionSource,
            shape = shape,
            colors = colors,
            border = border,
            content = content,
        )
    }
}

private fun Modifier.activateOnRemoteKey(
    enabled: Boolean,
    onClick: () -> Unit
): Modifier = onPreviewKeyEvent { event ->
    if (!enabled) return@onPreviewKeyEvent false
    val nativeEvent = event.nativeKeyEvent
    val isActivationKey = when (nativeEvent.keyCode) {
        KeyEvent.KEYCODE_DPAD_CENTER,
        KeyEvent.KEYCODE_ENTER,
        KeyEvent.KEYCODE_NUMPAD_ENTER,
        KeyEvent.KEYCODE_SPACE,
        KeyEvent.KEYCODE_BUTTON_A -> true
        else -> false
    }
    if (!isActivationKey) return@onPreviewKeyEvent false
    if (nativeEvent.action == KeyEvent.ACTION_UP) {
        onClick()
    }
    nativeEvent.action == KeyEvent.ACTION_DOWN || nativeEvent.action == KeyEvent.ACTION_UP
}
