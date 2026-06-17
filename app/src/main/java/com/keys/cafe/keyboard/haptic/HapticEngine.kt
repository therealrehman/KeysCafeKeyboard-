package com.keys.cafe.keyboard.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.keys.cafe.keyboard.model.KeyModel
import com.keys.cafe.keyboard.model.KeyboardSettings

/**
 * FIXED: HapticEngine - Now properly uses strength setting
 */
class HapticEngine(private val context: Context) {

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    // Settings
    var enabled: Boolean = true
    var strength: KeyboardSettings.VibrationStrength = KeyboardSettings.VibrationStrength.MEDIUM

    /**
     * FIXED: Perform haptic feedback for key press - now uses strength setting
     */
    fun performHaptic(key: KeyModel) {
        if (!enabled || !hasVibrator()) return

        // FIXED: Use strength setting to determine vibration intensity
        val effectiveVibration = if (key.vibrationTypeOrDefault == KeyModel.VibrationType.CUSTOM) {
            key.vibrationTypeOrDefault
        } else {
            // Map strength setting to vibration type
            when (strength) {
                KeyboardSettings.VibrationStrength.NONE -> KeyModel.VibrationType.NONE
                KeyboardSettings.VibrationStrength.LIGHT -> KeyModel.VibrationType.LIGHT
                KeyboardSettings.VibrationStrength.MEDIUM -> KeyModel.VibrationType.MEDIUM
                KeyboardSettings.VibrationStrength.STRONG -> KeyModel.VibrationType.STRONG
            }
        }

        when (effectiveVibration) {
            KeyModel.VibrationType.NONE -> return
            KeyModel.VibrationType.LIGHT -> performLightVibration()
            KeyModel.VibrationType.MEDIUM -> performMediumVibration()
            KeyModel.VibrationType.STRONG -> performStrongVibration()
            KeyModel.VibrationType.CUSTOM -> performCustomVibration()
        }
    }

    private fun performLightVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(10)
        }
    }

    private fun performMediumVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(25)
        }
    }

    private fun performStrongVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }

    private fun performCustomVibration() {
        val pattern = longArrayOf(0, 15, 5, 10)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    fun updateSettings(settings: KeyboardSettings) {
        enabled = settings.vibrationEnabled
        strength = settings.vibrationStrength
    }

    fun hasVibrator(): Boolean = vibrator.hasVibrator()
}
