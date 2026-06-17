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
import kotlinx.coroutines.launch

/**
 * FIXED: KeyboardView
 * - Added FastRepeat handling for backspace/space repeat
 * - Removed confusing swipe-down to settings
 * - Better error handling
 */
@Composable
fun KeyboardView(
    settings: KeyboardSettings = remember { KeyboardSettings() },
    onTextInput: (String) -> Unit = {},
    onDelete: () -> Unit = {},
    onEnter: () -> Unit = {},
    onSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Managers
    val layoutManager = remember { LayoutManager.getInstance(context) }
    val themeManager = remember { ThemeManager.getInstance(context) }
    val soundEngine = remember { SoundEngine(context) }
    val hapticEngine = remember { HapticEngine(context) }

    // State
    var currentLayout by remember { mutableStateOf<LayoutModel?>(null) }
    var currentTheme by remember { mutableStateOf<ThemeModel?>(null) }
    var shiftState by remember { mutableStateOf(ShiftState.OFF) }
    var isInitialized by remember { mutableStateOf(false) }

    // Initialize
    LaunchedEffect(Unit) {
        try {
            layoutManager.initialize()
            themeManager.initialize()
            currentLayout = layoutManager.getCurrentLayout()
            currentTheme = themeManager.getCurrentTheme()
            isInitialized = true
        } catch (e: Exception) {
            android.util.Log.e("KeysCafeKeyboard", "Failed to initialize keyboard", e)
        }
    }

    // Update engines with settings
    LaunchedEffect(settings) {
        soundEngine.updateSettings(settings)
        hapticEngine.updateSettings(settings)
    }

    /**
     * FIXED: Dispatch a key tap into real text-input / control actions
     */
    fun dispatchKey(key: KeyModel) {
        when (key.id) {
            "shift" -> {
                shiftState = when (shiftState) {
                    ShiftState.OFF -> ShiftState.SINGLE
                    ShiftState.SINGLE -> ShiftState.CAPS_LOCK
                    ShiftState.CAPS_LOCK -> ShiftState.OFF
                }
            }
            "backspace" -> onDelete()
            "enter" -> onEnter()
            "space" -> onTextInput(" ")
            "symbols" -> {
                scope.launch {
                    try {
                        if (layoutManager.switchLayout("symbols")) {
                            currentLayout = layoutManager.getCurrentLayout()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("KeysCafeKeyboard", "Failed to switch layout", e)
                    }
                }
            }
            "settings" -> onSettings()
            else -> {
                val isLetter = key.id.length == 1 && key.id.matches(Regex("[a-zA-Z]"))
                val output = if (isLetter && shiftState != ShiftState.OFF) {
                    key.output.uppercase()
                } else {
                    key.output
                }
                onTextInput(output)
                // FIXED: Reset single shift after typing
                if (shiftState == ShiftState.SINGLE) {
                    shiftState = ShiftState.OFF
                }
            }
        }
    }

    /**
     * FIXED: Touch event handler - now handles FastRepeat
     */
    val handleTouchEvent: (TouchEvent) -> Unit = { event ->
        when (event) {
            is TouchEvent.SingleTap -> dispatchKey(event.key)
            is TouchEvent.DoubleTap -> {
                // Double tap handling (future: caps lock toggle on shift)
                if (event.key.id == "shift") {
                    shiftState = ShiftState.CAPS_LOCK
                }
            }
            is TouchEvent.LongPress -> {
                if (event.key.supportsLongPress) {
                    // Show popup characters (future enhancement)
                }
            }
            // FIXED: Handle FastRepeat for repeatable keys
            is TouchEvent.FastRepeat -> {
                if (event.key.isRepeatable) {
                    dispatchKey(event.key)
                }
            }
            // FIXED: Handle PressStart and PressEnd for sound/haptic
            is TouchEvent.PressStart -> {
                soundEngine.playKeySound(event.key)
                hapticEngine.performHaptic(event.key)
            }
            is TouchEvent.PressEnd -> {
                // Release handling if needed
            }
            // FIXED: Removed SwipeDown -> onSettings (was confusing)
            // Swipe gestures reserved for future use
            is TouchEvent.SwipeLeft -> { /* Future: cursor movement */ }
            is TouchEvent.SwipeRight -> { /* Future: cursor movement */ }
            is TouchEvent.SwipeUp -> { /* Future: symbols */ }
            is TouchEvent.SwipeDown -> { /* Future: hide keyboard */ }
            is TouchEvent.MultiTouch -> { /* Future: multi-touch handling */ }
        }
    }

    // Key press / release handlers (sound + haptic feedback only)
    val handleKeyPress: (KeyModel) -> Unit = { key ->
        soundEngine.playKeySound(key)
        hapticEngine.performHaptic(key)
    }

    val handleKeyRelease: (KeyModel) -> Unit = { /* no-op for now */ }

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
            visible = currentLayout != null && isInitialized,
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
