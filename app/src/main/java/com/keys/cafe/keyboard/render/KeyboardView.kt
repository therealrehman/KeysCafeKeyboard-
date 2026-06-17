package com.keys.cafe.keyboard.render

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.keys.cafe.keyboard.haptic.HapticEngine
import com.keys.cafe.keyboard.layout.LayoutManager
import com.keys.cafe.keyboard.model.*
import com.keys.cafe.keyboard.sound.SoundEngine
import com.keys.cafe.keyboard.theme.ThemeManager
import com.keys.cafe.keyboard.touch.TouchEngine
import kotlinx.coroutines.launch

/**
 * Main keyboard view composable.
 * Integrates all components: layout, theme, touch, sound, haptic.
 */
@Composable
fun KeyboardView(
    settings: KeyboardSettings = remember { KeyboardSettings() }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Managers
    val layoutManager = remember { LayoutManager.getInstance(context) }
    val themeManager = remember { ThemeManager.getInstance(context) }
    val touchEngine = remember { TouchEngine(context) }
    val soundEngine = remember { SoundEngine(context) }
    val hapticEngine = remember { HapticEngine(context) }

    // State
    var currentLayout by remember { mutableStateOf<LayoutModel?>(null) }
    var currentTheme by remember { mutableStateOf<ThemeModel?>(null) }
    var shiftState by remember { mutableStateOf(ShiftState.OFF) }
    var keyboardMode by remember { mutableStateOf(LayoutModel.LayoutType.ALPHABET) }

    // Initialize
    LaunchedEffect(Unit) {
        layoutManager.initialize()
        themeManager.initialize()
        currentLayout = layoutManager.getCurrentLayout()
        currentTheme = themeManager.getCurrentTheme()
    }

    // Update engines with settings
    LaunchedEffect(settings) {
        soundEngine.updateSettings(settings)
        hapticEngine.updateSettings(settings)
        touchEngine.longPressDelay = settings.longPressDelay
    }

    // Touch event handler
    val handleTouchEvent: (TouchEvent) -> Unit = { event ->
        when (event) {
            is TouchEvent.SingleTap -> handleKeyInput(event.key, shiftState, soundEngine, hapticEngine) { newShift ->
                shiftState = newShift
            }
            is TouchEvent.LongPress -> {
                // Handle long press (popup characters, etc.)
                if (event.key.supportsLongPress) {
                    // Show popup
                }
            }
            is TouchEvent.SwipeUp -> {
                // Handle swipe gestures
            }
            is TouchEvent.SwipeDown -> {
                // Dismiss keyboard
            }
            else -> {} // Handle other events
        }
    }

    // Key press handler
    val handleKeyPress: (KeyModel) -> Unit = { key ->
        soundEngine.playKeySound(key)
        hapticEngine.performHaptic(key)
    }

    // Key release handler
    val handleKeyRelease: (KeyModel) -> Unit = { key ->
        // Cleanup if needed
    }

    // Layout switcher
    val switchLayout: (String) -> Unit = { layoutId ->
        scope.launch {
            if (layoutManager.switchLayout(layoutId)) {
                currentLayout = layoutManager.getCurrentLayout()
            }
        }
    }

    // Theme switcher
    val switchTheme: (String) -> Unit = { themeId ->
        scope.launch {
            if (themeManager.switchTheme(themeId)) {
                currentTheme = themeManager.getCurrentTheme()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
    ) {
        // Fire glow background effect
        if (currentTheme?.glowEnabled == true) {
            FireGlowBackground()
        }

        // Keyboard content
        AnimatedVisibility(
            visible = currentLayout != null,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            KeyboardRenderer(
                layout = currentLayout,
                theme = currentTheme,
                settings = settings,
                shiftState = shiftState,
                onKeyPress = handleKeyPress,
                onKeyRelease = handleKeyRelease,
                onTouchEvent = handleTouchEvent
            )
        }
    }
}

/**
 * Handle key input logic.
 */
private fun handleKeyInput(
    key: KeyModel,
    currentShift: ShiftState,
    soundEngine: SoundEngine,
    hapticEngine: HapticEngine,
    onShiftChange: (ShiftState) -> Unit
) {
    soundEngine.playKeySound(key)
    hapticEngine.performHaptic(key)

    when (key.id) {
        "shift" -> {
            val newShift = when (currentShift) {
                ShiftState.OFF -> ShiftState.SINGLE
                ShiftState.SINGLE -> ShiftState.CAPS_LOCK
                ShiftState.CAPS_LOCK -> ShiftState.OFF
            }
            onShiftChange(newShift)
        }
        "backspace" -> {
            // Handle backspace
        }
        "enter" -> {
            // Handle enter
        }
        "space" -> {
            // Handle space
        }
        "symbols" -> {
            // Switch to symbols
        }
        else -> {
            // Insert character
            val char = when {
                currentShift != ShiftState.OFF && key.id.length == 1 && key.id.matches(Regex("[a-z]")) -> {
                    key.id.uppercase()
                }
                else -> key.output
            }
            // Send to input connection
        }
    }
}

@Composable
private fun FireGlowBackground() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF5000).copy(alpha = 0.3f),
                        Color(0xFFFF8C00).copy(alpha = 0.15f),
                        Color(0xFFFFC800).copy(alpha = 0.05f),
                        Color.Transparent
                    )
                )
            )
    )
}
