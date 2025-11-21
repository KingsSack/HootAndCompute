package dev.kingssack.volt.util

import com.qualcomm.robotcore.util.ElapsedTime

class ButtonHandler(private val doubleTapThreshold: Double = 300.0) {
    var pressed = false

    private var lastPressTime = 0.0
    private var lastReleaseTime = 0.0
    private var tapCount = 0
    private var pressConsumed = false
    private var releaseConsumed = false

    private val runtime: ElapsedTime = ElapsedTime()

    fun update(buttonPressed: Boolean) {
        val currentTime = runtime.milliseconds()

        if (buttonPressed) {
            if (!pressed) {
                pressed = true
                pressConsumed = false // Reset press flag for new press action
                if (currentTime - lastReleaseTime < doubleTapThreshold) {
                    tapCount++
                } else {
                    tapCount = 1
                }
                lastPressTime = currentTime
            }
        } else {
            if (pressed) {
                pressed = false
                releaseConsumed = false // Reset release flag for new release action
                lastReleaseTime = currentTime
            }
        }
    }

    fun released(maxTimeMilliseconds: Double = 300.0): Boolean {
        if (
            !pressed &&
                runtime.milliseconds() - lastReleaseTime <= maxTimeMilliseconds &&
                !releaseConsumed
        ) {
            releaseConsumed = true
            return true
        }
        return false
    }

    fun tapped(): Boolean {
        if (pressed && tapCount == 1 && !pressConsumed) {
            pressConsumed = true
            return true
        }
        return false
    }

    fun doubleTapped(): Boolean {
        if (tapCount >= 2 && !pressed) { // Use >= to catch multiple taps if threshold is very short
            tapCount = 0 // Reset tap count after detecting a double tap (or more)
            return true
        }
        return false
    }

    fun held(milliseconds: Double): Boolean {
        return pressed && (runtime.milliseconds() - lastPressTime > milliseconds)
    }

    fun reset() {
        tapCount = 0
    }
}
