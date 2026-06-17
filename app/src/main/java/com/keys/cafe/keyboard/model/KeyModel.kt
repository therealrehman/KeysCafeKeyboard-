package com.keys.cafe.keyboard.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a single key on the keyboard.
 * This is the core data model for all keyboard keys.
 */
@Parcelize
data class KeyModel(
    val id: String,
    val label: String,
    val output: String,
    val width: Float = 1.0f,
    val height: Float = 1.0f,
    val weight: Float = 1.0f,
    val row: Int,
    val column: Int,
    val isModifier: Boolean = false,
    val isRepeatable: Boolean = false,
    val supportsLongPress: Boolean = false,
    val popupCharacters: List<String> = emptyList(),
    val accessibilityLabel: String = label,
    val soundType: SoundType = SoundType.STANDARD,
    val vibrationType: VibrationType = VibrationType.MEDIUM,
    val glowColor: GlowColor = GlowColor.DEFAULT
) : Parcelable {

    enum class SoundType {
        STANDARD, DELETE, ENTER, SPACE, SHIFT, SYMBOL, NONE
    }

    enum class VibrationType {
        NONE, LIGHT, MEDIUM, STRONG, CUSTOM
    }

    enum class GlowColor {
        DEFAULT, RED, BLUE, GREEN, YELLOW, PURPLE, CYAN, PINK, ORANGE, WHITE
    }
}
