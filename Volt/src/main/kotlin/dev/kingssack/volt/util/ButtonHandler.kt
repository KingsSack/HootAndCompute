package dev.kingssack.volt.util

import com.qualcomm.robotcore.util.ElapsedTime

class ButtonHandler(private val doubleTapThreshold: Double = 300.0) {
    var pressed = false

    private var lastPressTime = 0.0
    private var lastReleaseTime = 0.0
    private var tapCount = 0

    private val runtime: ElapsedTime = ElapsedTime()

    fun update(buttonPressed: Boolean) {
        val currentTime = runtime.milliseconds()

        if (buttonPressed) {
            if (!pressed) {
                pressed = true
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
                lastReleaseTime = currentTime
            }
        }
    }

    fun tapped(maxTimeMilliseconds: Double = 300.0): Boolean {
        if (tapCount == 1 && !pressed && runtime.milliseconds() - lastReleaseTime <= maxTimeMilliseconds) {
            tapCount = 0 // Reset tap count after detecting a tap
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
