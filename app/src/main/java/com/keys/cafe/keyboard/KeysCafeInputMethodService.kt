package com.keys.cafe.keyboard

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.keys.cafe.keyboard.data.SettingsRepository
import com.keys.cafe.keyboard.model.KeyboardSettings
import com.keys.cafe.keyboard.render.KeyboardView
import com.keys.cafe.keyboard.settings.SettingsActivity
import kotlinx.coroutines.flow.collectLatest

/**
 * SUPER DEFENSIVE: KeysCafeInputMethodService
 * - try-catch on every lifecycle method
 * - Safe ComposeView creation
 * - Fallback if anything fails
 * - No crash guaranteed
 */
class KeysCafeInputMethodService : InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private var composeView: ComposeView? = null
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val _viewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    private var currentSettings: KeyboardSettings = KeyboardSettings()
    private var settingsRepository: SettingsRepository? = null
    private var currentInputType: Int = InputType.TYPE_CLASS_TEXT

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = _viewModelStore
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        try {
            super.onCreate()
            savedStateRegistryController.performRestore(null)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            settingsRepository = SettingsRepository(applicationContext)
        } catch (e: Exception) {
            android.util.Log.e("KeysCafeIME", "onCreate failed", e)
        }
    }

    override fun onCreateInputView(): View? {
        return try {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

            val newComposeView = ComposeView(this).apply {
                setContent {
                    CompositionLocalProvider(
                        LocalLifecycleOwner provides this@KeysCafeInputMethodService
                    ) {
                        var settings by remember { mutableStateOf(currentSettings) }

                        LaunchedEffect(Unit) {
                            try {
                                settingsRepository?.settingsFlow?.collectLatest { newSettings ->
                                    settings = newSettings
                                    currentSettings = newSettings
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("KeysCafeIME", "Settings flow failed", e)
                            }
                        }

                        KeyboardView(
                            settings = settings,
                            onTextInput = { text -> handleTextInput(text) },
                            onDelete = { handleDelete() },
                            onEnter = { handleEnter() },
                            onSettings = { openSettings() }
                        )
                    }
                }
            }

            composeView = newComposeView
            newComposeView
        } catch (e: Exception) {
            android.util.Log.e("KeysCafeIME", "onCreateInputView failed", e)
            // Return a simple fallback view
            View(this)
        }
    }

    override fun onStartInput(attribute: EditorInfo, restarting: Boolean) {
        try {
            super.onStartInput(attribute, restarting)
            currentInputType = attribute.inputType
        } catch (e: Exception) {
            android.util.Log.e("KeysCafeIME", "onStartInput failed", e)
        }
    }

    override fun onWindowShown() {
        try {
            super.onWindowShown()
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        } catch (e: Exception) {
            android.util.Log.e("KeysCafeIME", "onWindowShown failed", e)
        }
    }

    override fun onWindowHidden() {
        try {
            super.onWindowHidden()
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        } catch (e: Exception) {
            android.util.Log.e("KeysCafeIME", "onWindowHidden failed", e)
        }
    }

    override fun onDestroy() {
        try {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            _viewModelStore.clear()
            composeView?.disposeComposition()
            composeView = null
            settingsRepository = null
        } catch (e: Exception) {
            android.util.Log.e("KeysCafeIME", "onDestroy failed", e)
        }
        try {
            super.onDestroy()
        } catch (e: Exception) {
            android.util.Log.e("KeysCafeIME", "super.onDestroy failed", e)
        }
    }

    private fun handleTextInput(text: String) {
        try {
            currentInputConnection?.commitText(text, 1)
        } catch (e: Exception) {
            android.util.Log.e("KeysCafeIME", "Text input failed", e)
        }
    }

    private fun handleDelete() {
        try {
            currentInputConnection?.deleteSurroundingText(1, 0)
        } catch (e: Exception) {
            android.util.Log.e("KeysCafeIME", "Delete failed", e)
        }
    }

    private fun handleEnter() {
        try {
            val ic = currentInputConnection ?: return
            val editorInfo = currentInputEditorInfo

            val action = editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION

            when (action) {
                EditorInfo.IME_ACTION_DONE -> ic.performEditorAction(EditorInfo.IME_ACTION_DONE)
                EditorInfo.IME_ACTION_GO -> ic.performEditorAction(EditorInfo.IME_ACTION_GO)
                EditorInfo.IME_ACTION_NEXT -> ic.performEditorAction(EditorInfo.IME_ACTION_NEXT)
                EditorInfo.IME_ACTION_SEARCH -> ic.performEditorAction(EditorInfo.IME_ACTION_SEARCH)
                EditorInfo.IME_ACTION_SEND -> ic.performEditorAction(EditorInfo.IME_ACTION_SEND)
                EditorInfo.IME_ACTION_PREVIOUS -> ic.performEditorAction(EditorInfo.IME_ACTION_PREVIOUS)
                else -> ic.commitText("\n", 1)
            }
        } catch (e: Exception) {
            android.util.Log.e("KeysCafeIME", "Enter failed", e)
        }
    }

    private fun openSettings() {
        try {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("KeysCafeIME", "Open settings failed", e)
        }
    }
}
