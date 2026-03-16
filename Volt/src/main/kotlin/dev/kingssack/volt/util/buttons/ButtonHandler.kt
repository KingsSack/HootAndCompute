package dev.kingssack.volt.util.buttons

import com.qualcomm.robotcore.util.ElapsedTime

/**
 * Handles button state updates and provides methods for detecting various button events.
 *
 * @param doubleTapThreshold the maximum time (ms) between two presses to count as a double tap
 * @param debounceThreshold the minimum time (ms) between state changes to prevent bouncing
 */
class ButtonHandler(
    private val doubleTapThreshold: Double = 300.0,
    private val debounceThreshold: Double = 50.0,
) {
    private var lastPressTime = 0.0
    private var lastReleaseTime = 0.0
    private var lastUpdateTime = 0.0
    private var tapCount = 0

    var pressed = false
        private set
    var tappedThisTick = false
        private set
    var releasedThisTick = false
        private set
    var doubleTappedThisTick = false
        private set

    private val runtime: ElapsedTime = ElapsedTime()

    /**
     * Updates the button state. Should be called in every loop iteration.
     *
     * @param buttonPressed The current raw state of the button from the gamepad.
     */
    fun update(buttonPressed: Boolean) {
        val currentTime = runtime.milliseconds()

        // Debounce check
        if (currentTime - lastUpdateTime < debounceThreshold) {
            return
        }

        tappedThisTick = false
        releasedThisTick = false
        doubleTappedThisTick = false

        if (buttonPressed != pressed) {
            lastUpdateTime = currentTime
            pressed = buttonPressed

            if (pressed) {
                tappedThisTick = true

                if (currentTime - lastReleaseTime < doubleTapThreshold) {
                    tapCount++

                    if (tapCount >= 2) {
                        tapCount = 0
                        doubleTappedThisTick = true
                    }
                } else {
                    tapCount = 1
                }
                lastPressTime = currentTime
            } else {
                releasedThisTick = true
                lastReleaseTime = currentTime
            }
        }
    }

    /** Checks if the button is currently held down for at least [milliseconds]. */
    fun held(milliseconds: Double): Boolean {
        return pressed && (runtime.milliseconds() - lastPressTime > milliseconds)
    }

    /** Resets the internal state (tap count, etc.). */
    fun reset() {
        tapCount = 0
        pressed = false
        tappedThisTick = false
        releasedThisTick = false
        doubleTappedThisTick = false
    }
}
