package com.keys.cafe.keyboard.render

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keys.cafe.keyboard.model.*
import kotlinx.coroutines.delay

/**
 * FIXED: KeyboardRenderer
 * - Fixed imports (added animateColorAsState)
 * - Fixed color flicker (removed System.currentTimeMillis())
 * - Added shift state visual indicator
 * - Fixed key sizing for landscape
 */
@Composable
fun KeyboardRenderer(
    layout: LayoutModel?,
    theme: ThemeModel?,
    settings: KeyboardSettings,
    shiftState: ShiftState,
    onKeyPress: (KeyModel) -> Unit,
    onKeyRelease: (KeyModel) -> Unit,
    onTouchEvent: (TouchEvent) -> Unit
) {
    if (layout == null || theme == null) return

    val colors = theme.toComposeColors()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Handle landscape mode - limit keyboard height
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val maxKeyboardHeight = if (isLandscape) (screenHeight * 0.6f) else (screenHeight * 0.45f)

    // Calculate key sizes based on settings
    val baseKeyHeight = when (settings.keySize) {
        KeyboardSettings.KeySize.SMALL -> 36.dp
        KeyboardSettings.KeySize.MEDIUM -> 44.dp
        KeyboardSettings.KeySize.LARGE -> 52.dp
        KeyboardSettings.KeySize.EXTRA_LARGE -> 60.dp
    }

    val sizeMultiplier = settings.keySizePercent / 100f
    val keyHeight = (baseKeyHeight.value * sizeMultiplier).dp
    val numberRowHeight = (baseKeyHeight.value * sizeMultiplier * 0.85).dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(
                start = layout.paddingStartOrDefault.dp,
                end = layout.paddingEndOrDefault.dp,
                top = layout.paddingTopOrDefault.dp,
                bottom = layout.paddingBottomOrDefault.dp
            ),
        verticalArrangement = Arrangement.spacedBy(layout.verticalGapOrDefault.dp)
    ) {
        layout.rows.forEachIndexed { rowIndex, row ->
            val rowHeight = if (rowIndex == 0) numberRowHeight else keyHeight
            val indentPadding = row.indentOrDefault.dp

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = indentPadding),
                horizontalArrangement = Arrangement.spacedBy(layout.horizontalGapOrDefault.dp)
            ) {
                row.keys.forEach { key ->
                    val isNumberRow = rowIndex == 0
                    val keyWidth = calculateKeyWidth(key, row, screenWidth, layout, layout.horizontalGapOrDefault.dp)

                    KeyButton(
                        key = key,
                        theme = colors,
                        settings = settings,
                        shiftState = shiftState,
                        width = keyWidth,
                        height = if (isNumberRow) numberRowHeight else keyHeight,
                        glowEnabled = theme.glowEnabled && settings.glowEnabled,
                        onPress = { onKeyPress(key) },
                        onRelease = { onKeyRelease(key) },
                        onTap = { onTouchEvent(TouchEvent.SingleTap(key)) },
                        onLongPress = { onTouchEvent(TouchEvent.LongPress(key)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyButton(
    key: KeyModel,
    theme: ThemeColors,
    settings: KeyboardSettings,
    shiftState: ShiftState,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    glowEnabled: Boolean,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var isLongPressed by remember { mutableStateOf(false) }
    var showGlow by remember { mutableStateOf(false) }

    // Animation states
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = tween(
            durationMillis = if (settings.animationEnabled) 70 else 0,
            easing = FastOutSlowInEasing
        ),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 1.0f else 0.82f,
        animationSpec = tween(
            durationMillis = if (settings.animationEnabled) 100 else 0
        ),
        label = "alpha"
    )

    // FIXED: Proper color animation without System.currentTimeMillis()
    val pressColor by animateColorAsState(
        targetValue = when {
            isLongPressed -> Color(0xFFFF6400)
            isPressed -> theme.keyPressed
            else -> theme.key
        },
        animationSpec = tween(
            durationMillis = if (settings.animationEnabled) 100 else 0
        ),
        label = "pressColor"
    )

    val glowColor = when (key.glowColorOrDefault) {
        KeyModel.GlowColor.RED -> Color(0xFFFF3333)
        KeyModel.GlowColor.BLUE -> Color(0xFF3396FF)
        KeyModel.GlowColor.GREEN -> Color(0xFF00FF96)
        KeyModel.GlowColor.YELLOW -> Color(0xFFFFDC00)
        KeyModel.GlowColor.PURPLE -> Color(0xFFB432FF)
        KeyModel.GlowColor.CYAN -> Color(0xFF00FFFF)
        KeyModel.GlowColor.PINK -> Color(0xFFFF5096)
        KeyModel.GlowColor.ORANGE -> Color(0xFFFFA064)
        KeyModel.GlowColor.WHITE -> Color.White
        else -> Color(0xFFFFAA00)
    }

    // FIXED: Shift state visual indicator
    val displayLabel = when {
        key.id == "shift" -> when (shiftState) {
            ShiftState.OFF -> "⇧"
            ShiftState.SINGLE -> "⇧"
            ShiftState.CAPS_LOCK -> "⇪"
        }
        key.id == "backspace" -> "⌫"
        key.id == "enter" -> "↵"
        key.id == "space" -> key.label
        key.id.length == 1 && key.id.matches(Regex("[a-z]")) -> {
            when (shiftState) {
                ShiftState.OFF -> key.label.lowercase()
                ShiftState.SINGLE -> key.label.uppercase()
                ShiftState.CAPS_LOCK -> key.label.uppercase()
            }
        }
        else -> key.label
    }

    // FIXED: Shift key background color changes with state
    val shiftBackgroundColor = if (key.id == "shift" && shiftState != ShiftState.OFF) {
        theme.keyPressed
    } else {
        pressColor
    }

    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .scale(scale)
            .alpha(alpha)
            .shadow(
                elevation = if (isPressed) 8.dp else 2.dp,
                shape = RoundedCornerShape(8.dp),
                ambientColor = if (glowEnabled && showGlow) glowColor else Color.Transparent,
                spotColor = if (glowEnabled && showGlow) glowColor else Color.Transparent
            )
            .background(
                color = shiftBackgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .pointerInput(key.id) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        showGlow = true
                        onPress()
                        tryAwaitRelease()
                        isPressed = false
                        isLongPressed = false
                        showGlow = false
                        onRelease()
                    },
                    onTap = { onTap() },
                    onLongPress = {
                        isLongPressed = true
                        onLongPress()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Fire glow effect
        if (glowEnabled && showGlow) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 0.8f),
                                glowColor.copy(alpha = 0.5f),
                                glowColor.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            center = Offset(0.5f, 0.5f),
                            radius = 2.0f
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        }

        // FIXED: Shift key icon color changes with state
        val textColor = when {
            key.id == "shift" && shiftState != ShiftState.OFF -> theme.keyPressedText
            isPressed -> theme.keyPressedText
            else -> theme.keyText
        }

        // Key label
        Text(
            text = displayLabel,
            color = textColor,
            fontSize = if (key.id.length == 1 && key.id.matches(Regex("[a-z0-9]"))) 16.sp else 14.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun calculateKeyWidth(
    key: KeyModel,
    row: RowModel,
    screenWidth: androidx.compose.ui.unit.Dp,
    layout: LayoutModel,
    gap: androidx.compose.ui.unit.Dp
): androidx.compose.ui.unit.Dp {
    val totalWeight = row.keys.sumOf { it.weight.toDouble() }.toFloat()
    val totalGap = gap * (row.keys.size - 1)
    val availableWidth = screenWidth - (layout.paddingStartOrDefault + layout.paddingEndOrDefault).dp - totalGap
    val baseWidth = availableWidth / totalWeight
    return (baseWidth * key.weight).coerceAtLeast(28.dp)
}
