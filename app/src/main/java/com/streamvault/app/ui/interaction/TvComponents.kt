package com.streamvault.app.ui.interaction

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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import com.streamvault.app.ui.design.LocalAppColors
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
    shape: ClickableSurfaceShape = ClickableSurfaceDefaults.shape(),
    colors: ClickableSurfaceColors = ClickableSurfaceDefaults.colors(),
    border: ClickableSurfaceBorder = ClickableSurfaceDefaults.border(),
    scale: ClickableSurfaceScale = ClickableSurfaceDefaults.scale(),
    glow: ClickableSurfaceGlow = ClickableSurfaceDefaults.glow(),
    interactionSource: MutableInteractionSource? = null,
    onLongClick: (() -> Unit)? = null,
    cornerRadius: androidx.compose.ui.unit.Dp = 12.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }
    val colorsPalette = LocalAppColors.current
    val isLightGlass = colorsPalette == com.streamvault.app.ui.design.GlassLightAppColors
    val isGlass = colorsPalette == com.streamvault.app.ui.design.GlassLightAppColors || colorsPalette == com.streamvault.app.ui.design.GlassDarkAppColors

    val resolvedColors = if (isGlass) {
        ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            pressedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            contentColor = colors.contentColor,
            focusedContentColor = colors.focusedContentColor,
            pressedContentColor = colors.pressedContentColor,
            disabledContentColor = colors.disabledContentColor
        )
    } else {
        ClickableSurfaceDefaults.colors(
            containerColor = colorsPalette.surface,
            focusedContainerColor = colorsPalette.surfaceAccent,
            pressedContainerColor = colorsPalette.surfaceEmphasis,
            disabledContainerColor = colorsPalette.surface.copy(alpha = 0.5f),
            contentColor = colors.contentColor,
            focusedContentColor = colors.focusedContentColor,
            pressedContentColor = colors.pressedContentColor,
            disabledContentColor = colors.disabledContentColor
        )
    }

    val resolvedBorder = if (isGlass) {
        ClickableSurfaceDefaults.border(
            border = Border.None,
            focusedBorder = Border.None,
            pressedBorder = Border.None,
            disabledBorder = Border.None
        )
    } else {
        border
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
                }

                drawContent()

                if (isGlass && isFocused && enabled) {
                    val currentCorner = cornerRadius.toPx()
                    val innerCorner = (cornerRadius - 1.dp).coerceAtLeast(0.dp).toPx()
                    
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
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(currentCorner, currentCorner),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx())
                    )

                    // 3. Volumetric double-edge refraction lighting (stacked strokes)
                    // Outer fine edge light
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
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(currentCorner, currentCorner),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                    )
                    
                    // Inner refraction border (thick volumetric reflection)
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = if (isLightGlass) {
                                listOf(
                                    Color.White.copy(alpha = 0.50f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.08f)
                                )
                            } else {
                                listOf(
                                    Color.White.copy(alpha = 0.30f),
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.15f)
                                )
                            }
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(innerCorner, innerCorner),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                    )
                }
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
    colors: ButtonColors = ButtonDefaults.colors(),
    border: ButtonBorder = ButtonDefaults.border(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    val colorsPalette = LocalAppColors.current
    val isGlass = colorsPalette == com.streamvault.app.ui.design.GlassLightAppColors || colorsPalette == com.streamvault.app.ui.design.GlassDarkAppColors

    if (isGlass) {
        val focusRequester = remember { FocusRequester() }
        TvClickableSurface(
            onClick = onClick,
            modifier = modifier
                .focusRequester(focusRequester)
                .mouseClickable(onClick = onClick, enabled = enabled),
            enabled = enabled,
            cornerRadius = 20.dp,
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
    colors: ButtonColors = IconButtonDefaults.colors(),
    border: ButtonBorder = IconButtonDefaults.border(),
    content: @Composable BoxScope.() -> Unit,
) {
    val colorsPalette = LocalAppColors.current
    val isGlass = colorsPalette == com.streamvault.app.ui.design.GlassLightAppColors || colorsPalette == com.streamvault.app.ui.design.GlassDarkAppColors

    if (isGlass) {
        val focusRequester = remember { FocusRequester() }
        TvClickableSurface(
            onClick = onClick,
            onLongClick = onLongClick,
            modifier = modifier
                .focusRequester(focusRequester)
                .mouseClickable(onClick = onClick, enabled = enabled, onLongClick = onLongClick),
            enabled = enabled,
            cornerRadius = 28.dp,
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
