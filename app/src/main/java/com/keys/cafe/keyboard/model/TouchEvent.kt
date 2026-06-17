package com.keys.cafe.keyboard.model

/**
 * Represents different touch events on keyboard keys.
 */
sealed class TouchEvent {
    data class SingleTap(val key: KeyModel) : TouchEvent()
    data class DoubleTap(val key: KeyModel) : TouchEvent()
    data class LongPress(val key: KeyModel) : TouchEvent()
    data class SwipeLeft(val key: KeyModel) : TouchEvent()
    data class SwipeRight(val key: KeyModel) : TouchEvent()
    data class SwipeUp(val key: KeyModel) : TouchEvent()
    data class SwipeDown(val key: KeyModel) : TouchEvent()
    data class MultiTouch(val keys: List<KeyModel>) : TouchEvent()
    data class FastRepeat(val key: KeyModel) : TouchEvent()
    data class PressStart(val key: KeyModel) : TouchEvent()
    data class PressEnd(val key: KeyModel) : TouchEvent()
}
