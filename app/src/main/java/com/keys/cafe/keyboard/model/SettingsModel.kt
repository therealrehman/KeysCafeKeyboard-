package com.keys.cafe.keyboard.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * User preferences and settings.
 */
@Parcelize
data class KeyboardSettings(
    val currentLayout: String = "qwerty",
    val currentTheme: String = "fire",
    val keySize: KeySize = KeySize.MEDIUM,
    val keySizePercent: Int = 100,
    val soundEnabled: Boolean = true,
    val soundVolume: Int = 80,
    val vibrationEnabled: Boolean = true,
    val vibrationStrength: VibrationStrength = VibrationStrength.MEDIUM,
    val animationEnabled: Boolean = true,
    val animationSpeed: AnimationSpeed = AnimationSpeed.NORMAL,
    val glowEnabled: Boolean = true,
    val showPopup: Boolean = true,
    val accessibilityMode: Boolean = false,
    val highContrast: Boolean = false,
    val largeKeys: Boolean = false,
    val colorBlindMode: Boolean = false,
    val language: String = "en-US",
    val autoCapitalize: Boolean = true,
    val doubleTapSpacePeriod: Boolean = true,
    val longPressDelay: Long = 400L,
    val swipeEnabled: Boolean = true
) : Parcelable {

    enum class KeySize {
        SMALL, MEDIUM, LARGE, EXTRA_LARGE
    }

    enum class VibrationStrength {
        NONE, LIGHT, MEDIUM, STRONG
    }

    enum class AnimationSpeed {
        SLOW, NORMAL, FAST
    }
}
