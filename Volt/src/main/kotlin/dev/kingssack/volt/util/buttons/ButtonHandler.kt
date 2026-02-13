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
    /** Current state of the button (true if pressed, false if released). */
    var pressed = false
        private set

    /** Toggles state on each press. */
    var toggle = false
        private set

    private var lastPressTime = 0.0
    private var lastReleaseTime = 0.0
    private var lastUpdateTime = 0.0
    private var tapCount = 0
    private var pressConsumed = false
    private var releaseConsumed = true // Initialize to true so we don't trigger release on startup

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

        if (buttonPressed != pressed) {
            lastUpdateTime = currentTime
            pressed = buttonPressed

            if (pressed) {
                // Button just pressed
                pressConsumed = false
                toggle = !toggle

                if (currentTime - lastReleaseTime < doubleTapThreshold) {
                    tapCount++
                } else {
                    tapCount = 1
                }
                lastPressTime = currentTime
            } else {
                // Button just released
                releaseConsumed = false
                lastReleaseTime = currentTime
            }
        }
    }

    /**
     * Checks if the button was just pressed (rising edge). Consumes the event so it returns true
     * only once per press.
     */
    fun justPressed(): Boolean {
        if (pressed && !pressConsumed) {
            pressConsumed = true
            return true
        }
        return false
    }

    /**
     * Checks if the button was just released (falling edge). Consumes the event so it returns true
     * only once per release.
     */
    fun justReleased(): Boolean {
        if (!pressed && !releaseConsumed) {
            releaseConsumed = true
            return true
        }
        return false
    }

    /**
     * Checks if the button was released within the last [maxTimeMilliseconds]. Note: This consumes
     * the release event, so it cannot be used in conjunction with [justReleased] for the same
     * event.
     */
    fun released(maxTimeMilliseconds: Double = 300.0): Boolean {
        if (
            !pressed &&
                !releaseConsumed &&
                (runtime.milliseconds() - lastReleaseTime <= maxTimeMilliseconds)
        ) {
            releaseConsumed = true
            return true
        }
        return false
    }

    /** Checks if the button was double tapped. */
    fun doubleTapped(): Boolean {
        if (tapCount >= 2 && !pressed) {
            tapCount = 0
            return true
        }
        return false
    }

    /** Checks if the button is currently held down for at least [milliseconds]. */
    fun held(milliseconds: Double): Boolean {
        return pressed && (runtime.milliseconds() - lastPressTime > milliseconds)
    }

    /**
     * Checks if the button is currently held down for at least [milliseconds]. Alias for [held] for
     * clarity.
     */
    fun isHeld(milliseconds: Double): Boolean = held(milliseconds)

    /** Resets the internal state (tap count, etc). */
    fun reset() {
        tapCount = 0
        toggle = false
        pressed = false
        pressConsumed = false
        releaseConsumed = true
    }
}
