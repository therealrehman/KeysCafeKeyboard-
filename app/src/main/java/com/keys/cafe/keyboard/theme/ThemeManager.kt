package com.keys.cafe.keyboard.theme

import android.content.Context
import com.google.gson.Gson
import com.keys.cafe.keyboard.model.ThemeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Manages keyboard themes: loading, switching, and applying themes.
 */
class ThemeManager(private val context: Context) {

    private val gson = Gson()
    private val themeCache = mutableMapOf<String, ThemeModel>()
    private val availableThemes = listOf("fire", "dark", "light", "amoled")

    private var currentThemeId: String = "fire"
    private var currentTheme: ThemeModel? = null

    companion object {
        @Volatile
        private var instance: ThemeManager? = null

        fun getInstance(context: Context): ThemeManager {
            return instance ?: synchronized(this) {
                instance ?: ThemeManager(context.applicationContext).also { instance = it }
            }
        }
    }

    suspend fun initialize() = withContext(Dispatchers.IO) {
        availableThemes.forEach { themeId ->
            loadTheme(themeId)
        }
        currentTheme = themeCache["fire"]
    }

    suspend fun loadTheme(themeId: String): ThemeModel? = withContext(Dispatchers.IO) {
        themeCache[themeId] ?: try {
            val json = context.assets.open("themes/$themeId.json").bufferedReader().use { it.readText() }
            val theme = gson.fromJson(json, ThemeModel::class.java)
            themeCache[themeId] = theme
            theme
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun switchTheme(themeId: String): Boolean {
        val theme = themeCache[themeId] ?: return false
        currentThemeId = themeId
        currentTheme = theme
        return true
    }

    fun getCurrentTheme(): ThemeModel? = currentTheme
    fun getCurrentThemeId(): String = currentThemeId
    fun getAvailableThemes(): List<String> = availableThemes

    suspend fun getTheme(themeId: String): ThemeModel? {
        return themeCache[themeId] ?: loadTheme(themeId)
    }

    fun clearCache() {
        themeCache.clear()
        currentTheme = null
    }

    fun destroy() {
        clearCache()
        instance = null
    }
}
