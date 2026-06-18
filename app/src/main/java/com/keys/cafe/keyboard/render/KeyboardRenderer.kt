package com.keys.cafe.keyboard.render

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import kotlinx.coroutines.launch

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
    if (layout == null || theme == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading keyboard...", color = Color.Gray)
        }
        return
    }

    val colors = theme.toComposeColors()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    val baseKeyHeight = when (settings.keySize) {
        KeyboardSettings.KeySize.SMALL -> 40
        KeyboardSettings.KeySize.MEDIUM -> 48
        KeyboardSettings.KeySize.LARGE -> 56
        KeyboardSettings.KeySize.EXTRA_LARGE -> 64
    }

    val sizeMultiplier = settings.keySizePercent / 100f
    val keyHeightVal = (baseKeyHeight * sizeMultiplier).toInt()
    val numberRowHeightVal = (baseKeyHeight * sizeMultiplier * 0.88f).toInt()

    val keyHeight = keyHeightVal.dp
    val numberRowHeight = numberRowHeightVal.dp
    val horizontalGap = 5.dp
    val verticalGap = 6.dp
    val sidePadding = 4.dp

    // Calculate available width in pixels
    val sidePaddingPx = with(density) { sidePadding.toPx() }
    val gapPx = with(density) { horizontalGap.toPx() }
    val screenWidthPx = with(density) { screenWidthDp.dp.toPx() }
    val availableWidthPx = screenWidthPx - (sidePaddingPx * 2f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.background)
                .padding(horizontal = sidePadding, vertical = 8.dp)
        ) {
            if (theme.glowEnabled && settings.glowEnabled) {
                FireGlowEffect()
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(verticalGap)
            ) {
                layout.rows.forEachIndexed { rowIndex, row ->
                    val isNumberRow = rowIndex == 0
                    val rowH = if (isNumberRow) numberRowHeight else keyHeight

                    val totalWeight = row.keys.sumOf { it.weight.toDouble() }.toFloat()
                    val gapCount = maxOf(0, row.keys.size - 1)
                    val totalGapWidth = gapCount * gapPx
                    val rowAvailableWidth = availableWidthPx - totalGapWidth
                    val unitWidthPx = rowAvailableWidth / totalWeight

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = row.indentOrDefault.dp),
                        horizontalArrangement = Arrangement.spacedBy(horizontalGap)
                    ) {
                        row.keys.forEach { key ->
                            val keyWidthPx = unitWidthPx * key.weight
                            val keyWidth = with(density) { keyWidthPx.toDp() }

                            KeyButton(
                                key = key,
                                theme = colors,
                                settings = settings,
                                shiftState = shiftState,
                                width = keyWidth,
                                height = rowH,
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
    var animStep by remember { mutableStateOf(-1) }
    val scope = rememberCoroutineScope()

    val animColors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFF00FFFF),
        Color(0xFFFF00AA),
        Color(0xFFFF6400)
    )

    val currentBg = if (animStep >= 0) animColors[animStep] else theme.key
    val currentText = if (animStep in 0..1) Color.Black else if (animStep in 2..3) Color.White else theme.keyText

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = tween(durationMillis = 60, easing = FastOutSlowInEasing),
        label = "scale"
    )

    val bgColor by animateColorAsState(
        targetValue = currentBg,
        animationSpec = tween(durationMillis = 50),
        label = "bg"
    )

    val textColor by animateColorAsState(
        targetValue = currentText,
        animationSpec = tween(durationMillis = 50),
        label = "text"
    )

    val glowColor = try {
        when (key.glowColorOrDefault) {
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
    } catch (e: Exception) { Color(0xFFFFAA00) }

    val displayLabel = try {
        when {
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
                    else -> key.label.uppercase()
                }
            }
            else -> key.label
        }
    } catch (e: Exception) { key.id }

    fun startAnimation() {
        if (!settings.animationEnabled) return
        scope.launch {
            animStep = 0; delay(70)
            animStep = 1; delay(70)
            animStep = 2; delay(70)
            animStep = 3; delay(180)
            animStep = -1
        }
    }

    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .scale(scale)
            .shadow(
                elevation = if (isPressed && glowEnabled) 8.dp else 1.dp,
                shape = RoundedCornerShape(8.dp),
                ambientColor = if (glowEnabled) glowColor else Color.Transparent,
                spotColor = if (glowEnabled) glowColor else Color.Transparent
            )
            .background(bgColor, RoundedCornerShape(8.dp))
            .pointerInput(key.id) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onPress()
                        startAnimation()
                        tryAwaitRelease()
                        isPressed = false
                        onRelease()
                    },
                    onTap = { onTap() },
                    onLongPress = { onLongPress() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (isPressed && glowEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 0.5f),
                                glowColor.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            center = Offset(0.5f, 0.5f),
                            radius = 0.8f
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        }

        Text(
            text = displayLabel,
            color = textColor,
            fontSize = if (key.id.length == 1 && key.id.matches(Regex("[a-z0-9]"))) 17.sp else 13.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun FireGlowEffect() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFFF5000).copy(alpha = 0.15f),
                        Color(0xFFFF8C00).copy(alpha = 0.3f),
                        Color(0xFFFFA500).copy(alpha = 0.2f),
                        Color(0xFFFFC800).copy(alpha = 0.1f)
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    )
}
