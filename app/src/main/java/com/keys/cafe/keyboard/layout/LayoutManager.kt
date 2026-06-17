package com.keys.cafe.keyboard.layout

import android.content.Context
import com.google.gson.Gson
import com.keys.cafe.keyboard.model.KeyModel
import com.keys.cafe.keyboard.model.LayoutModel
import com.keys.cafe.keyboard.model.RowModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Manages keyboard layouts: loading, switching, caching, and rendering preparation.
 * 
 * Responsibilities:
 * - Load layout from JSON files
 * - Switch layout dynamically without restart
 * - Cache layouts for instant switching
 * - Provide layout data to rendering engine
 */
class LayoutManager(private val context: Context) {

    private val gson = Gson()
    private val layoutCache = mutableMapOf<String, LayoutModel>()
    private val availableLayouts = listOf(
        "qwerty", "qwertz", "azerty", "dvorak", "colemak", "numbers", "symbols"
    )

    private var currentLayoutId: String = "qwerty"
    private var currentLayout: LayoutModel? = null

    companion object {
        @Volatile
        private var instance: LayoutManager? = null

        fun getInstance(context: Context): LayoutManager {
            return instance ?: synchronized(this) {
                instance ?: LayoutManager(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Initialize and preload all layouts.
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        availableLayouts.forEach { layoutId ->
            loadLayout(layoutId)
        }
        // Set default
        currentLayout = layoutCache["qwerty"]
    }

    /**
     * Load a layout from assets and cache it.
     */
    suspend fun loadLayout(layoutId: String): LayoutModel? = withContext(Dispatchers.IO) {
        layoutCache[layoutId] ?: try {
            val json = context.assets.open("layouts/$layoutId.json").bufferedReader().use { it.readText() }
            val layout = gson.fromJson(json, LayoutModel::class.java)
            layoutCache[layoutId] = layout
            layout
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Switch to a different layout instantly.
     */
    fun switchLayout(layoutId: String): Boolean {
        val layout = layoutCache[layoutId] ?: return false
        currentLayoutId = layoutId
        currentLayout = layout
        return true
    }

    /**
     * Get current active layout.
     */
    fun getCurrentLayout(): LayoutModel? = currentLayout

    /**
     * Get current layout ID.
     */
    fun getCurrentLayoutId(): String = currentLayoutId

    /**
     * Get all available layout IDs.
     */
    fun getAvailableLayouts(): List<String> = availableLayouts

    /**
     * Get layout by ID (from cache or load).
     */
    suspend fun getLayout(layoutId: String): LayoutModel? {
        return layoutCache[layoutId] ?: loadLayout(layoutId)
    }

    /**
     * Get key at specific position (for touch detection).
     */
    fun getKeyAtPosition(row: Int, column: Int): KeyModel? {
        return currentLayout?.rows?.getOrNull(row)?.keys?.getOrNull(column)
    }

    /**
     * Find key by ID in current layout.
     */
    fun findKeyById(keyId: String): KeyModel? {
        return currentLayout?.rows?.flatMap { it.keys }?.find { it.id == keyId }
    }

    /**
     * Get all keys in current layout.
     */
    fun getAllKeys(): List<KeyModel> {
        return currentLayout?.rows?.flatMap { it.keys } ?: emptyList()
    }

    /**
     * Get modifier keys (shift, backspace, enter, etc.).
     */
    fun getModifierKeys(): List<KeyModel> {
        return getAllKeys().filter { it.isModifier }
    }

    /**
     * Clear cache (for memory management).
     */
    fun clearCache() {
        layoutCache.clear()
        currentLayout = null
    }

    /**
     * Reload current layout (for theme/setting changes).
     */
    suspend fun reloadCurrentLayout() {
        layoutCache.remove(currentLayoutId)
        loadLayout(currentLayoutId)
        switchLayout(currentLayoutId)
    }

    /**
     * Destroy and cleanup.
     */
    fun destroy() {
        clearCache()
        instance = null
    }
}
