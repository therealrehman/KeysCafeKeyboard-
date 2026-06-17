package com.keys.cafe.keyboard.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.keys.cafe.keyboard.layout.LayoutManager
import com.keys.cafe.keyboard.theme.ThemeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Centralized error handling and recovery system.
 * 
 * Handles:
 * - Layout Missing
 * - Theme Corrupted
 * - Keyboard Crash Recovery
 * - Invalid Key Definition
 * - Touch Failure
 * - Memory Overflow
 */
class ErrorHandler(private val context: Context) {

    companion object {
        private const val TAG = "KeysCafeError"
    }

    /**
     * Handle layout missing error.
     */
    fun handleLayoutMissing(layoutId: String) {
        Log.e(TAG, "Layout missing: $layoutId")
        showError("Layout file missing: $layoutId")

        // Fallback to qwerty
        CoroutineScope(Dispatchers.IO).launch {
            LayoutManager.getInstance(context).switchLayout("qwerty")
        }
    }

    /**
     * Handle theme corrupted error.
     */
    fun handleThemeCorrupted(themeId: String) {
        Log.e(TAG, "Theme corrupted: $themeId")
        showError("Theme file corrupted: $themeId")

        // Fallback to dark theme
        ThemeManager.getInstance(context).switchTheme("dark")
    }

    /**
     * Handle keyboard crash recovery.
     */
    fun handleCrashRecovery() {
        Log.w(TAG, "Keyboard recovered from crash")
        showError("Keyboard recovered from crash")

        // Reset to safe defaults
        CoroutineScope(Dispatchers.IO).launch {
            val layoutManager = LayoutManager.getInstance(context)
            val themeManager = ThemeManager.getInstance(context)

            layoutManager.clearCache()
            themeManager.clearCache()

            layoutManager.initialize()
            themeManager.initialize()

            layoutManager.switchLayout("qwerty")
            themeManager.switchTheme("dark")
        }
    }

    /**
     * Handle invalid key definition.
     */
    fun handleInvalidKey(keyId: String, layoutId: String) {
        Log.e(TAG, "Invalid key definition: $keyId in layout $layoutId")
        // Silently ignore invalid keys
    }

    /**
     * Handle touch failure.
     */
    fun handleTouchFailure(exception: Exception) {
        Log.e(TAG, "Touch processing failed", exception)
        // Don't show error to user for touch failures
    }

    /**
     * Handle memory overflow.
     */
    fun handleMemoryOverflow() {
        Log.e(TAG, "Memory limit reached")
        showError("Memory limit reached. Clearing cache...")

        // Clear caches
        CoroutineScope(Dispatchers.IO).launch {
            LayoutManager.getInstance(context).clearCache()
            ThemeManager.getInstance(context).clearCache()
            System.gc()
        }
    }

    /**
     * Generic error handler.
     */
    fun handleError(error: Throwable, message: String? = null) {
        Log.e(TAG, message ?: "Unknown error", error)
        message?.let { showError(it) }
    }

    private fun showError(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
