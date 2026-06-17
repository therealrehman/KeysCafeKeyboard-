package com.keys.cafe.keyboard.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a single key on the keyboard.
 * This is the core data model for all keyboard keys.
 *
 * Note: fields that may be absent from layout JSON use nullable backing
 * properties (vibrationTypeRaw, glowColorRaw, accessibilityLabelRaw) with
 * non-null computed accessors. Gson's reflection-based deserializer does
 * NOT honor Kotlin default parameter values, so any field missing from
 * JSON must be nullable to avoid silent corruption/crashes.
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
    private val accessibilityLabel: String? = null,
    val soundType: SoundType = SoundType.STANDARD,
    private val vibrationType: VibrationType? = null,
    private val glowColor: GlowColor? = null
) : Parcelable {

    val accessibilityLabelOrDefault: String
        get() = accessibilityLabel ?: label

    val vibrationTypeOrDefault: VibrationType
        get() = vibrationType ?: VibrationType.MEDIUM

    val glowColorOrDefault: GlowColor
        get() = glowColor ?: GlowColor.DEFAULT

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
