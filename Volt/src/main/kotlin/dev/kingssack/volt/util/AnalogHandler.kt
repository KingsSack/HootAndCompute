package dev.kingssack.volt.util

import kotlin.math.abs
import kotlin.math.pow

class AnalogHandler(private val deadzone: Float = 0.1f, private val inputExp: Float = 2.0f) {
    /**
     * Processes an input with deadzone and exponential scaling.
     *
     * @param input the input to process
     * @return the processed input
     */
    private fun processInput(input: Float): Float {
        // Apply deadzone
        if (abs(input) < deadzone) return 0.0f

        // Normalize input
        val normalizedInput = (input - deadzone) / (1.0f - deadzone)

        // Apply exponential scaling for fine control
        return normalizedInput.pow(inputExp) * if (input < 0.0f) -1.0f else 1.0f
    }

    var value: Float = 0.0f

    fun update(input: Float) {
        value = processInput(input)
    }
}
