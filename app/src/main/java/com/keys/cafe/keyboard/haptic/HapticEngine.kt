package com.keys.cafe.keyboard.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.keys.cafe.keyboard.model.KeyModel
import com.keys.cafe.keyboard.model.KeyboardSettings

/**
 * Manages haptic feedback for keyboard.
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
     * Perform haptic feedback for key press.
     */
    fun performHaptic(key: KeyModel) {
        if (!enabled) return

        when (key.vibrationType) {
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
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(20)
        }
    }

    private fun performStrongVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(40)
        }
    }

    private fun performCustomVibration() {
        // Custom vibration pattern
        val pattern = longArrayOf(0, 15, 5, 10)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    /**
     * Update settings.
     */
    fun updateSettings(settings: KeyboardSettings) {
        enabled = settings.vibrationEnabled
        strength = settings.vibrationStrength
    }

    /**
     * Check if vibrator is available.
     */
    fun hasVibrator(): Boolean = vibrator.hasVibrator()
}
