package com.keys.cafe.keyboard.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.keys.cafe.keyboard.model.KeyboardSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "keyboard_settings")

/**
 * Repository for managing keyboard settings using DataStore.
 */
class SettingsRepository(private val context: Context) {

    private val dataStore = context.dataStore

    // Keys
    private val CURRENT_LAYOUT = stringPreferencesKey("current_layout")
    private val CURRENT_THEME = stringPreferencesKey("current_theme")
    private val KEY_SIZE = stringPreferencesKey("key_size")
    private val KEY_SIZE_PERCENT = intPreferencesKey("key_size_percent")
    private val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    private val SOUND_VOLUME = intPreferencesKey("sound_volume")
    private val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
    private val VIBRATION_STRENGTH = stringPreferencesKey("vibration_strength")
    private val ANIMATION_ENABLED = booleanPreferencesKey("animation_enabled")
    private val GLOW_ENABLED = booleanPreferencesKey("glow_enabled")
    private val SHOW_POPUP = booleanPreferencesKey("show_popup")
    private val ACCESSIBILITY_MODE = booleanPreferencesKey("accessibility_mode")
    private val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
    private val LARGE_KEYS = booleanPreferencesKey("large_keys")
    private val COLOR_BLIND_MODE = booleanPreferencesKey("color_blind_mode")
    private val LANGUAGE = stringPreferencesKey("language")
    private val AUTO_CAPITALIZE = booleanPreferencesKey("auto_capitalize")
    private val DOUBLE_TAP_SPACE = booleanPreferencesKey("double_tap_space")
    private val LONG_PRESS_DELAY = longPreferencesKey("long_press_delay")
    private val SWIPE_ENABLED = booleanPreferencesKey("swipe_enabled")

    val settingsFlow: Flow<KeyboardSettings> = dataStore.data.map { preferences ->
        KeyboardSettings(
            currentLayout = preferences[CURRENT_LAYOUT] ?: "qwerty",
            currentTheme = preferences[CURRENT_THEME] ?: "fire",
            keySize = KeyboardSettings.KeySize.valueOf(
                preferences[KEY_SIZE] ?: "MEDIUM"
            ),
            keySizePercent = preferences[KEY_SIZE_PERCENT] ?: 100,
            soundEnabled = preferences[SOUND_ENABLED] ?: true,
            soundVolume = preferences[SOUND_VOLUME] ?: 80,
            vibrationEnabled = preferences[VIBRATION_ENABLED] ?: true,
            vibrationStrength = KeyboardSettings.VibrationStrength.valueOf(
                preferences[VIBRATION_STRENGTH] ?: "MEDIUM"
            ),
            animationEnabled = preferences[ANIMATION_ENABLED] ?: true,
            animationSpeed = KeyboardSettings.AnimationSpeed.NORMAL,
            glowEnabled = preferences[GLOW_ENABLED] ?: true,
            showPopup = preferences[SHOW_POPUP] ?: true,
            accessibilityMode = preferences[ACCESSIBILITY_MODE] ?: false,
            highContrast = preferences[HIGH_CONTRAST] ?: false,
            largeKeys = preferences[LARGE_KEYS] ?: false,
            colorBlindMode = preferences[COLOR_BLIND_MODE] ?: false,
            language = preferences[LANGUAGE] ?: "en-US",
            autoCapitalize = preferences[AUTO_CAPITALIZE] ?: true,
            doubleTapSpacePeriod = preferences[DOUBLE_TAP_SPACE] ?: true,
            longPressDelay = preferences[LONG_PRESS_DELAY] ?: 400L,
            swipeEnabled = preferences[SWIPE_ENABLED] ?: true
        )
    }

    suspend fun updateSettings(settings: KeyboardSettings) {
        dataStore.edit { preferences ->
            preferences[CURRENT_LAYOUT] = settings.currentLayout
            preferences[CURRENT_THEME] = settings.currentTheme
            preferences[KEY_SIZE] = settings.keySize.name
            preferences[KEY_SIZE_PERCENT] = settings.keySizePercent
            preferences[SOUND_ENABLED] = settings.soundEnabled
            preferences[SOUND_VOLUME] = settings.soundVolume
            preferences[VIBRATION_ENABLED] = settings.vibrationEnabled
            preferences[VIBRATION_STRENGTH] = settings.vibrationStrength.name
            preferences[ANIMATION_ENABLED] = settings.animationEnabled
            preferences[GLOW_ENABLED] = settings.glowEnabled
            preferences[SHOW_POPUP] = settings.showPopup
            preferences[ACCESSIBILITY_MODE] = settings.accessibilityMode
            preferences[HIGH_CONTRAST] = settings.highContrast
            preferences[LARGE_KEYS] = settings.largeKeys
            preferences[COLOR_BLIND_MODE] = settings.colorBlindMode
            preferences[LANGUAGE] = settings.language
            preferences[AUTO_CAPITALIZE] = settings.autoCapitalize
            preferences[DOUBLE_TAP_SPACE] = settings.doubleTapSpacePeriod
            preferences[LONG_PRESS_DELAY] = settings.longPressDelay
            preferences[SWIPE_ENABLED] = settings.swipeEnabled
        }
    }

    suspend fun resetToDefaults() {
        dataStore.edit { it.clear() }
    }
}
