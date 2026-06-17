package com.keys.cafe.keyboard.touch

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.VelocityTracker
import com.keys.cafe.keyboard.model.KeyModel
import com.keys.cafe.keyboard.model.TouchEvent
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Advanced touch engine for keyboard input.
 * 
 * Detects:
 * - Single Tap
 * - Double Tap
 * - Long Press
 * - Swipe (Left, Right, Up, Down)
 * - Multi Touch
 * - Fast Repeated Taps
 */
class TouchEngine(private val context: Context) {

    private val handler = Handler(Looper.getMainLooper())
    private var velocityTracker: VelocityTracker? = null

    // Touch state
    private var activePointerId: Int = -1
    private var touchStartX: Float = 0f
    private var touchStartY: Float = 0f
    private var touchStartTime: Long = 0
    private var lastTapTime: Long = 0
    private var tapCount: Int = 0
    private var currentKey: KeyModel? = null
    private var isLongPressTriggered = false
    private var isSwipeDetected = false

    // Configuration
    var longPressDelay: Long = 400L
    var swipeThreshold: Float = 80f
    var doubleTapTimeout: Long = 300L
    var fastRepeatThreshold: Long = 100L

    // Callbacks
    var onTouchEvent: ((TouchEvent) -> Unit)? = null
    var onKeyPress: ((KeyModel) -> Unit)? = null
    var onKeyRelease: ((KeyModel) -> Unit)? = null

    // Runnables
    private val longPressRunnable = Runnable {
        currentKey?.let { key ->
            isLongPressTriggered = true
            onTouchEvent?.invoke(TouchEvent.LongPress(key))
        }
    }

    private val fastRepeatRunnable = Runnable {
        currentKey?.let { key ->
            if (key.isRepeatable) {
                onTouchEvent?.invoke(TouchEvent.FastRepeat(key))
            }
        }
    }

    /**
     * Process motion event from the keyboard view.
     */
    fun onTouchEvent(event: MotionEvent, keyFinder: (Float, Float) -> KeyModel?): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                return handleActionDown(event, keyFinder)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                return handlePointerDown(event, keyFinder)
            }
            MotionEvent.ACTION_MOVE -> {
                return handleActionMove(event, keyFinder)
            }
            MotionEvent.ACTION_UP -> {
                return handleActionUp(event)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                return handlePointerUp(event)
            }
            MotionEvent.ACTION_CANCEL -> {
                return handleActionCancel()
            }
        }
        return false
    }

    private fun handleActionDown(event: MotionEvent, keyFinder: (Float, Float) -> KeyModel?): Boolean {
        activePointerId = event.getPointerId(0)
        touchStartX = event.x
        touchStartY = event.y
        touchStartTime = System.currentTimeMillis()

        velocityTracker = VelocityTracker.obtain()
        velocityTracker?.addMovement(event)

        currentKey = keyFinder(event.x, event.y)
        currentKey?.let { key ->
            onKeyPress?.invoke(key)
            onTouchEvent?.invoke(TouchEvent.PressStart(key))
            handler.postDelayed(longPressRunnable, longPressDelay)
        }

        return true
    }

    private fun handlePointerDown(event: MotionEvent, keyFinder: (Float, Float) -> KeyModel?): Boolean {
        val pointerIndex = event.actionIndex
        val pointerId = event.getPointerId(pointerIndex)

        val x = event.getX(pointerIndex)
        val y = event.getY(pointerIndex)

        val key = keyFinder(x, y)
        key?.let {
            onTouchEvent?.invoke(TouchEvent.MultiTouch(listOfNotNull(currentKey, key)))
        }

        return true
    }

    private fun handleActionMove(event: MotionEvent, keyFinder: (Float, Float) -> KeyModel?): Boolean {
        velocityTracker?.addMovement(event)
        velocityTracker?.computeCurrentVelocity(1000)

        val dx = event.x - touchStartX
        val dy = event.y - touchStartY
        val distance = sqrt(dx * dx + dy * dy)

        // Detect swipe
        if (!isSwipeDetected && !isLongPressTriggered && distance > swipeThreshold) {
            isSwipeDetected = true
            handler.removeCallbacks(longPressRunnable)

            currentKey?.let { key ->
                val swipeEvent = when {
                    abs(dx) > abs(dy) && dx > 0 -> TouchEvent.SwipeRight(key)
                    abs(dx) > abs(dy) && dx < 0 -> TouchEvent.SwipeLeft(key)
                    abs(dy) > abs(dx) && dy < 0 -> TouchEvent.SwipeUp(key)
                    else -> TouchEvent.SwipeDown(key)
                }
                onTouchEvent?.invoke(swipeEvent)
            }
        }

        // Detect key change during swipe
        if (isSwipeDetected) {
            val newKey = keyFinder(event.x, event.y)
            if (newKey != currentKey && newKey != null) {
                currentKey?.let { onKeyRelease?.invoke(it) }
                currentKey = newKey
                onKeyPress?.invoke(newKey)
            }
        }

        return true
    }

    private fun handleActionUp(event: MotionEvent): Boolean {
        handler.removeCallbacks(longPressRunnable)
        handler.removeCallbacks(fastRepeatRunnable)

        val touchDuration = System.currentTimeMillis() - touchStartTime
        val timeSinceLastTap = System.currentTimeMillis() - lastTapTime

        currentKey?.let { key ->
            onKeyRelease?.invoke(key)
            onTouchEvent?.invoke(TouchEvent.PressEnd(key))

            if (!isLongPressTriggered && !isSwipeDetected) {
                if (timeSinceLastTap < doubleTapTimeout && tapCount > 0) {
                    // Double tap
                    tapCount = 0
                    onTouchEvent?.invoke(TouchEvent.DoubleTap(key))
                } else {
                    // Single tap
                    tapCount = 1
                    onTouchEvent?.invoke(TouchEvent.SingleTap(key))

                    // Check for fast repeat
                    if (touchDuration < fastRepeatThreshold) {
                        handler.postDelayed(fastRepeatRunnable, fastRepeatThreshold)
                    }
                }
            }
        }

        lastTapTime = System.currentTimeMillis()
        resetState()
        return true
    }

    private fun handlePointerUp(event: MotionEvent): Boolean {
        return true
    }

    private fun handleActionCancel(): Boolean {
        handler.removeCallbacks(longPressRunnable)
        handler.removeCallbacks(fastRepeatRunnable)
        currentKey?.let { onKeyRelease?.invoke(it) }
        resetState()
        return true
    }

    private fun resetState() {
        activePointerId = -1
        isLongPressTriggered = false
        isSwipeDetected = false
        currentKey = null
        velocityTracker?.recycle()
        velocityTracker = null
    }

    /**
     * Destroy and cleanup resources.
     */
    fun destroy() {
        handler.removeCallbacksAndMessages(null)
        velocityTracker?.recycle()
        velocityTracker = null
    }
}
