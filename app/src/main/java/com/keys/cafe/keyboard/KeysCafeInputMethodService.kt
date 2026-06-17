package com.keys.cafe.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.*
import com.keys.cafe.keyboard.data.SettingsRepository
import com.keys.cafe.keyboard.model.KeyboardSettings
import com.keys.cafe.keyboard.render.KeyboardView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Android Input Method Service for KeysCafe Keyboard.
 * 
 * This is the core IME that integrates with the Android system.
 */
class KeysCafeInputMethodService : InputMethodService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private lateinit var composeView: ComposeView
    private val dispatcher = androidx.lifecycle.LifecycleEventDispatcher()
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val viewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    private var currentText: StringBuilder = StringBuilder()
    private var settings: KeyboardSettings = KeyboardSettings()
    private lateinit var settingsRepository: SettingsRepository

    override val lifecycle: Lifecycle = lifecycleRegistry
    override val viewModelStore: ViewModelStore = viewModelStore
    override val savedStateRegistry: SavedStateRegistry = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        settingsRepository = SettingsRepository(applicationContext)
    }

    override fun onCreateInputView(): View {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        composeView = ComposeView(this).apply {
            setContent {
                var currentSettings by remember { mutableStateOf(settings) }

                // Collect settings changes
                LaunchedEffect(Unit) {
                    settingsRepository.settingsFlow.collectLatest { newSettings ->
                        currentSettings = newSettings
                        settings = newSettings
                    }
                }

                KeyboardView(
                    settings = currentSettings,
                    onTextInput = { text -> handleTextInput(text) },
                    onDelete = { handleDelete() },
                    onEnter = { handleEnter() },
                    onSettings = { openSettings() }
                )
            }
        }

        return composeView
    }

    override fun onStartInput(attribute: EditorInfo, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        // Reset state for new input field
        currentText.clear()
    }

    override fun onFinishInput() {
        super.onFinishInput()
        currentText.clear()
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }

    /**
     * Handle text input from keyboard.
     */
    private fun handleTextInput(text: String) {
        val inputConnection = currentInputConnection ?: return
        currentText.append(text)
        inputConnection.commitText(text, 1)
    }

    /**
     * Handle backspace/delete.
     */
    private fun handleDelete() {
        val inputConnection = currentInputConnection ?: return
        if (currentText.isNotEmpty()) {
            currentText.deleteCharAt(currentText.length - 1)
        }
        inputConnection.deleteSurroundingText(1, 0)
    }

    /**
     * Handle enter key.
     */
    private fun handleEnter() {
        val inputConnection = currentInputConnection ?: return
        val editorInfo = currentInputEditorInfo

        when (editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION) {
            EditorInfo.IME_ACTION_DONE -> inputConnection.performEditorAction(EditorInfo.IME_ACTION_DONE)
            EditorInfo.IME_ACTION_GO -> inputConnection.performEditorAction(EditorInfo.IME_ACTION_GO)
            EditorInfo.IME_ACTION_NEXT -> inputConnection.performEditorAction(EditorInfo.IME_ACTION_NEXT)
            EditorInfo.IME_ACTION_SEARCH -> inputConnection.performEditorAction(EditorInfo.IME_ACTION_SEARCH)
            EditorInfo.IME_ACTION_SEND -> inputConnection.performEditorAction(EditorInfo.IME_ACTION_SEND)
            else -> inputConnection.commitText("\n", 1)
        }
    }

    /**
     * Open keyboard settings.
     */
    private fun openSettings() {
        // Launch settings activity
        val intent = android.content.Intent(this, settings.SettingsActivity::class.java)
        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    /**
     * Get current input connection safely.
     */
    private val currentInputConnection: InputConnection?
        get() = currentInputConnection
}
