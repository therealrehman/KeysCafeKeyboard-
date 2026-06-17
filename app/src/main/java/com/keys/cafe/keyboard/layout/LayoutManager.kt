package com.keys.cafe.keyboard.layout

import android.content.Context
import com.google.gson.Gson
import com.keys.cafe.keyboard.model.KeyModel
import com.keys.cafe.keyboard.model.LayoutModel
import com.keys.cafe.keyboard.model.RowModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * FIXED: LayoutManager
 * - Memory leak fixed with proper cleanup
 * - Better error handling
 * - Added destroy() method called from service
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

        /**
         * FIXED: Public method to clear instance
         */
        fun destroyInstance() {
            instance?.clearCache()
            instance = null
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
        } catch (e: Exception) {
            android.util.Log.e("KeysCafeLayout", "Failed to load layout '$layoutId'", e)
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

    fun getCurrentLayout(): LayoutModel? = currentLayout
    fun getCurrentLayoutId(): String = currentLayoutId
    fun getAvailableLayouts(): List<String> = availableLayouts

    suspend fun getLayout(layoutId: String): LayoutModel? {
        return layoutCache[layoutId] ?: loadLayout(layoutId)
    }

    fun getKeyAtPosition(row: Int, column: Int): KeyModel? {
        return currentLayout?.rows?.getOrNull(row)?.keys?.getOrNull(column)
    }

    fun findKeyById(keyId: String): KeyModel? {
        return currentLayout?.rows?.flatMap { it.keys }?.find { it.id == keyId }
    }

    fun getAllKeys(): List<KeyModel> {
        return currentLayout?.rows?.flatMap { it.keys } ?: emptyList()
    }

    fun getModifierKeys(): List<KeyModel> {
        return getAllKeys().filter { it.isModifier }
    }

    fun clearCache() {
        layoutCache.clear()
        currentLayout = null
    }

    suspend fun reloadCurrentLayout() {
        layoutCache.remove(currentLayoutId)
        loadLayout(currentLayoutId)
        switchLayout(currentLayoutId)
    }

    /**
     * FIXED: Proper destroy with instance cleanup
     */
    fun destroy() {
        clearCache()
        destroyInstance()
    }
}
