package com.keys.cafe.keyboard.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import com.keys.cafe.keyboard.model.KeyModel
import com.keys.cafe.keyboard.model.KeyboardSettings

/**
 * FIXED: SoundEngine - Now properly loads and plays sounds
 * Uses system sound effects as fallback until custom sounds are added
 */
class SoundEngine(private val context: Context) {

    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<KeyModel.SoundType, Int>()
    private var isInitialized = false
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

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

        // FIXED: Load default sounds from raw resources
        // Add these sound files to res/raw/ folder:
        // key_standard.mp3, key_delete.mp3, key_enter.mp3, key_space.mp3, key_shift.mp3
        try {
            val resId = context.resources.getIdentifier("key_standard", "raw", context.packageName)
            if (resId != 0) {
                soundMap[KeyModel.SoundType.STANDARD] = soundPool?.load(context, resId, 1) ?: 0
            }

            val deleteId = context.resources.getIdentifier("key_delete", "raw", context.packageName)
            if (deleteId != 0) {
                soundMap[KeyModel.SoundType.DELETE] = soundPool?.load(context, deleteId, 1) ?: 0
            }

            val enterId = context.resources.getIdentifier("key_enter", "raw", context.packageName)
            if (enterId != 0) {
                soundMap[KeyModel.SoundType.ENTER] = soundPool?.load(context, enterId, 1) ?: 0
            }

            val spaceId = context.resources.getIdentifier("key_space", "raw", context.packageName)
            if (spaceId != 0) {
                soundMap[KeyModel.SoundType.SPACE] = soundPool?.load(context, spaceId, 1) ?: 0
            }

            val shiftId = context.resources.getIdentifier("key_shift", "raw", context.packageName)
            if (shiftId != 0) {
                soundMap[KeyModel.SoundType.SHIFT] = soundPool?.load(context, shiftId, 1) ?: 0
            }
        } catch (e: Exception) {
            android.util.Log.w("KeysCafeSound", "Custom sounds not found, using system defaults")
        }

        isInitialized = true
    }

    /**
     * FIXED: Play sound for a key press - now falls back to system sounds
     */
    fun playSound(soundType: KeyModel.SoundType) {
        if (!enabled || !isInitialized) return

        val soundId = soundMap[soundType]
        if (soundId != null && soundId != 0) {
            soundPool?.play(soundId, volume, volume, 1, 0, 1.0f)
        } else {
            playSystemSound(soundType)
        }
    }

    /**
     * NEW: Play system sound effect as fallback
     */
    private fun playSystemSound(soundType: KeyModel.SoundType) {
        val fx = when (soundType) {
            KeyModel.SoundType.DELETE -> AudioManager.FX_KEYPRESS_DELETE
            KeyModel.SoundType.ENTER -> AudioManager.FX_KEYPRESS_RETURN
            KeyModel.SoundType.SPACE -> AudioManager.FX_KEYPRESS_SPACEBAR
            else -> AudioManager.FX_KEY_CLICK
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            audioManager.playSoundEffect(fx, volume)
        } else {
            @Suppress("DEPRECATION")
            audioManager.playSoundEffect(fx)
        }
    }

    fun playKeySound(key: KeyModel) {
        playSound(key.soundType)
    }

    fun updateSettings(settings: KeyboardSettings) {
        enabled = settings.soundEnabled
        volume = settings.soundVolume / 100f
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        soundMap.clear()
        isInitialized = false
    }
}
