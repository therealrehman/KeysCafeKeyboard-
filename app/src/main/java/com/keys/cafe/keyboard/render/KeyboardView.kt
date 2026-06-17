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
 * SUPER DEFENSIVE: KeyboardView
 * - try-catch on every operation
 * - Safe initialization
 * - Fallback if managers fail
 * - No crash guaranteed
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

    // State
    var currentLayout by remember { mutableStateOf<LayoutModel?>(null) }
    var currentTheme by remember { mutableStateOf<ThemeModel?>(null) }
    var shiftState by remember { mutableStateOf(ShiftState.OFF) }
    var isInitialized by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // DEFENSIVE: Initialize with try-catch
    LaunchedEffect(Unit) {
        try {
            val layoutManager = LayoutManager.getInstance(context)
            val themeManager = ThemeManager.getInstance(context)

            layoutManager.initialize()
            themeManager.initialize()

            currentLayout = layoutManager.getCurrentLayout()
            currentTheme = themeManager.getCurrentTheme()
            isInitialized = true
        } catch (e: Exception) {
            android.util.Log.e("KeysCafeKeyboard", "Failed to initialize keyboard", e)
            errorMessage = "Keyboard init failed: ${e.message}"
        }
    }

    // DEFENSIVE: Show error if initialization failed
    if (errorMessage != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFF1A0000)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Keyboard Error: ${errorMessage}",
                color = Color(0xFFFF3333),
                fontSize = 12.sp
            )
        }
        return
    }

    // DEFENSIVE: Show loading if not initialized
    if (!isInitialized) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading KeysCafe Keyboard...", color = Color(0xFFFFAA00))
        }
        return
    }

    // DEFENSIVE: Create engines with try-catch
    val soundEngine = remember {
        try { SoundEngine(context) } catch (e: Exception) { null }
    }
    val hapticEngine = remember {
        try { HapticEngine(context) } catch (e: Exception) { null }
    }

    LaunchedEffect(settings) {
        try {
            soundEngine?.updateSettings(settings)
            hapticEngine?.updateSettings(settings)
        } catch (e: Exception) {
            android.util.Log.e("KeysCafeKeyboard", "Settings update failed", e)
        }
    }

    fun dispatchKey(key: KeyModel) {
        try {
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
                            val layoutManager = LayoutManager.getInstance(context)
                            if (layoutManager.switchLayout("symbols")) {
                                currentLayout = layoutManager.getCurrentLayout()
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("KeysCafeKeyboard", "Layout switch failed", e)
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
                    if (shiftState == ShiftState.SINGLE) {
                        shiftState = ShiftState.OFF
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("KeysCafeKeyboard", "Key dispatch failed", e)
        }
    }

    val handleTouchEvent: (TouchEvent) -> Unit = { event ->
        try {
            when (event) {
                is TouchEvent.SingleTap -> dispatchKey(event.key)
                is TouchEvent.DoubleTap -> {
                    if (event.key.id == "shift") {
                        shiftState = ShiftState.CAPS_LOCK
                    }
                }
                is TouchEvent.LongPress -> {
                    // Long press handling
                }
                is TouchEvent.FastRepeat -> {
                    if (event.key.isRepeatable) {
                        dispatchKey(event.key)
                    }
                }
                is TouchEvent.PressStart -> {
                    try { soundEngine?.playKeySound(event.key) } catch (e: Exception) {}
                    try { hapticEngine?.performHaptic(event.key) } catch (e: Exception) {}
                }
                else -> {}
            }
        } catch (e: Exception) {
            android.util.Log.e("KeysCafeKeyboard", "Touch event failed", e)
        }
    }

    val handleKeyPress: (KeyModel) -> Unit = { key ->
        try { soundEngine?.playKeySound(key) } catch (e: Exception) {}
        try { hapticEngine?.performHaptic(key) } catch (e: Exception) {}
    }

    val handleKeyRelease: (KeyModel) -> Unit = { }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
    ) {
        if (currentTheme?.glowEnabled == true) {
            FireGlowBackgroundSafe()
        }

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
private fun FireGlowBackgroundSafe() {
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
