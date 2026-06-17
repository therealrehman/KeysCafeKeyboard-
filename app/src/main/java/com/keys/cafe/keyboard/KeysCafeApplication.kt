package com.keys.cafe.keyboard

import android.app.Application
import com.keys.cafe.keyboard.data.SettingsRepository

/**
 * Application class for KeysCafe Keyboard.
 */
class KeysCafeApplication : Application() {

    lateinit var settingsRepository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        settingsRepository = SettingsRepository(applicationContext)
    }

    companion object {
        lateinit var instance: KeysCafeApplication
            private set
    }
}
