package com.keys.cafe.keyboard.model

import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import kotlinx.parcelize.Parcelize

/**
 * Represents a complete keyboard theme.
 */
@Parcelize
data class ThemeModel(
    val id: String,
    val name: String,
    val type: ThemeType,
    val backgroundColor: Long = 0xFF000000,
    val keyColor: Long = 0xFF080808,
    val keyPressedColor: Long = 0xFFFFAA00,
    val keyTextColor: Long = 0xFF885500,
    val keyPressedTextColor: Long = 0xFF000000,
    val borderColor: Long = 0xFF111111,
    val shadowColor: Long = 0xFF000000,
    val cornerRadius: Float = 8f,
    val glowEnabled: Boolean = true,
    val glowIntensity: Float = 0.8f,
    val animationSpeed: Float = 1.0f
) : Parcelable {

    enum class ThemeType {
        LIGHT, DARK, AMOLED, FIRE, CUSTOM
    }

    fun toComposeColors(): ThemeColors {
        return ThemeColors(
            background = Color(backgroundColor),
            key = Color(keyColor),
            keyPressed = Color(keyPressedColor),
            keyText = Color(keyTextColor),
            keyPressedText = Color(keyPressedTextColor),
            border = Color(borderColor),
            shadow = Color(shadowColor)
        )
    }
}

data class ThemeColors(
    val background: Color,
    val key: Color,
    val keyPressed: Color,
    val keyText: Color,
    val keyPressedText: Color,
    val border: Color,
    val shadow: Color
)
