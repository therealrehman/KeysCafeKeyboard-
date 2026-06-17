package com.keys.cafe.keyboard.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Vibrator
import com.keys.cafe.keyboard.model.KeyModel
import com.keys.cafe.keyboard.model.KeyboardSettings

/**
 * Manages keyboard sound effects.
 */
class SoundEngine(private val context: Context) {

    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<KeyModel.SoundType, Int>()
    private var isInitialized = false

    // Settings
    var enabled: Boolean = true
    var volume: Float = 0.8f

    init {
        initialize()
    }

    private fun initialize() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Load default sounds (would load from raw resources in production)
        // For now, we'll use system defaults
        isInitialized = true
    }

    /**
     * Play sound for a key press.
     */
    fun playSound(soundType: KeyModel.SoundType) {
        if (!enabled || !isInitialized) return

        val soundId = soundMap[soundType] ?: return
        soundPool?.play(soundId, volume, volume, 1, 0, 1.0f)
    }

    /**
     * Play key-specific sound.
     */
    fun playKeySound(key: KeyModel) {
        playSound(key.soundType)
    }

    /**
     * Update settings.
     */
    fun updateSettings(settings: KeyboardSettings) {
        enabled = settings.soundEnabled
        volume = settings.soundVolume / 100f
    }

    /**
     * Release resources.
     */
    fun release() {
        soundPool?.release()
        soundPool = null
        isInitialized = false
    }
}
