package com.keys.cafe.keyboard.model

/**
 * Represents the visual state of a key.
 */
enum class KeyState {
    IDLE,
    PRESSED,
    LONG_PRESSED,
    DISABLED,
    HIGHLIGHTED,
    SELECTED,
    ANIMATING
}

/**
 * Shift key states.
 */
enum class ShiftState {
    OFF,
    SINGLE,
    CAPS_LOCK
}
