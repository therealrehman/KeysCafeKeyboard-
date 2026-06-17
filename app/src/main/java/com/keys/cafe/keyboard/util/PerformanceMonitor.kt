package com.keys.cafe.keyboard.util

import android.util.Log
import kotlin.system.measureNanoTime

/**
 * Performance monitoring utility.
 * 
 * Tracks:
 * - Keyboard open time
 * - Touch response time
 * - Memory usage
 * - Frame rate
 * - Battery impact
 */
class PerformanceMonitor {

    companion object {
        private const val TAG = "KeysCafePerf"

        // Performance thresholds
        const val MAX_OPEN_TIME_MS = 50L
        const val MAX_TOUCH_RESPONSE_MS = 10L
        const val MAX_MEMORY_MB = 100L
        const val TARGET_FPS = 60
    }

    private var frameCount = 0
    private var lastFrameTime = 0L
    private var currentFps = 0

    /**
     * Measure keyboard open time.
     */
    inline fun measureOpenTime(block: () -> Unit): Long {
        val timeMs = measureNanoTime(block) / 1_000_000
        if (timeMs > MAX_OPEN_TIME_MS) {
            Log.w(TAG, "Keyboard open time exceeded: ${timeMs}ms (max: ${MAX_OPEN_TIME_MS}ms)")
        } else {
            Log.d(TAG, "Keyboard open time: ${timeMs}ms")
        }
        return timeMs
    }

    /**
     * Measure touch response time.
     */
    inline fun measureTouchResponse(block: () -> Unit): Long {
        val timeMs = measureNanoTime(block) / 1_000_000
        if (timeMs > MAX_TOUCH_RESPONSE_MS) {
            Log.w(TAG, "Touch response exceeded: ${timeMs}ms (max: ${MAX_TOUCH_RESPONSE_MS}ms)")
        }
        return timeMs
    }

    /**
     * Track frame rate.
     */
    fun onFrameRendered() {
        val currentTime = System.currentTimeMillis()
        frameCount++

        if (currentTime - lastFrameTime >= 1000) {
            currentFps = frameCount
            frameCount = 0
            lastFrameTime = currentTime

            if (currentFps < TARGET_FPS - 5) {
                Log.w(TAG, "Frame rate dropped: $currentFps FPS (target: $TARGET_FPS)")
            }
        }
    }

    /**
     * Get current memory usage in MB.
     */
    fun getMemoryUsage(): Long {
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)

        if (usedMemory > MAX_MEMORY_MB) {
            Log.w(TAG, "Memory usage high: ${usedMemory}MB (max: ${MAX_MEMORY_MB}MB)")
        }
        return usedMemory
    }

    /**
     * Get current FPS.
     */
    fun getCurrentFps(): Int = currentFps

    /**
     * Reset all metrics.
     */
    fun reset() {
        frameCount = 0
        lastFrameTime = 0
        currentFps = 0
    }
}
