package com.keys.cafe.keyboard.accessibility

import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.keys.cafe.keyboard.model.KeyModel

/**
 * Accessibility helper for TalkBack and screen reader support.
 */
class AccessibilityHelper(private val context: Context) {

    private val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

    /**
     * Check if TalkBack is enabled.
     */
    fun isTalkBackEnabled(): Boolean {
        return accessibilityManager.isEnabled && accessibilityManager.isTouchExplorationEnabled
    }

    /**
     * Announce text to screen reader.
     */
    fun announce(text: String) {
        if (!isTalkBackEnabled()) return

        val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        event.text.add(text)
        accessibilityManager.sendAccessibilityEvent(event)
    }

    /**
     * Get accessibility description for a key.
     */
    fun getKeyDescription(key: KeyModel): String {
        return when (key.id) {
            "shift" -> "Shift key"
            "backspace" -> "Backspace key, delete character"
            "enter" -> "Enter key, new line"
            "space" -> "Space key"
            "symbols" -> "Symbols key, switch to symbols"
            else -> key.accessibilityLabel
        }
    }

    /**
     * Create accessibility node info for a key.
     */
    fun createKeyNodeInfo(key: KeyModel): AccessibilityNodeInfoCompat {
        val nodeInfo = AccessibilityNodeInfoCompat.obtain()
        nodeInfo.text = getKeyDescription(key)
        nodeInfo.isClickable = true
        nodeInfo.isFocusable = true
        return nodeInfo
    }
}
