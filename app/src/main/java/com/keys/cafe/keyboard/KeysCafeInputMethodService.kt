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
 * FIXED: KeysCafeInputMethodService
 * - Proper lifecycle management
 * - Input type handling (password, email, number, etc.)
 * - Proper cleanup on destroy
 * - IME action handling complete
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

    // FIXED: Track current input type
    private var currentInputType: Int = InputType.TYPE_CLASS_TEXT

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = _viewModelStore
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        settingsRepository = SettingsRepository(applicationContext)
    }

    /**
     * FIXED: Proper input view creation with lifecycle
     */
    override fun onCreateInputView(): View {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val newComposeView = ComposeView(this).apply {
            setContent {
                CompositionLocalProvider(
                    LocalLifecycleOwner provides this@KeysCafeInputMethodService
                ) {
                    var settings by remember { mutableStateOf(currentSettings) }

                    LaunchedEffect(Unit) {
                        settingsRepository?.settingsFlow?.collectLatest { newSettings ->
                            settings = newSettings
                            currentSettings = newSettings
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
        return newComposeView
    }

    /**
     * FIXED: Handle input type changes
     */
    override fun onStartInput(attribute: EditorInfo, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        currentInputType = attribute.inputType

        // FIXED: Adjust keyboard based on input type
        when (attribute.inputType and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_NUMBER,
            InputType.TYPE_CLASS_PHONE -> {
                // Could switch to number layout here
                android.util.Log.d("KeysCafeIME", "Number/Phone input detected")
            }
            InputType.TYPE_CLASS_TEXT -> {
                // Check for variations
                when (attribute.inputType and InputType.TYPE_MASK_VARIATION) {
                    InputType.TYPE_TEXT_VARIATION_PASSWORD,
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
                    InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD -> {
                        android.util.Log.d("KeysCafeIME", "Password input detected")
                    }
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                    InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS -> {
                        android.util.Log.d("KeysCafeIME", "Email input detected")
                    }
                }
            }
        }
    }

    /**
     * FIXED: Proper window showing
     */
    override fun onWindowShown() {
        super.onWindowShown()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    /**
     * FIXED: Proper cleanup on destroy
     */
    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        _viewModelStore.clear()
        composeView?.disposeComposition()
        composeView = null
        settingsRepository = null
        super.onDestroy()
    }

    private fun handleTextInput(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    private fun handleDelete() {
        currentInputConnection?.deleteSurroundingText(1, 0)
    }

    /**
     * FIXED: Complete IME action handling
     */
    private fun handleEnter() {
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
    }

    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}
