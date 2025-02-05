package dev.kingssack.volt.util

import kotlin.math.abs
import kotlin.math.pow

class AnalogHandler(private val deadzone: Double = 0.1, private val inputExp: Double = 2.0) {
    /**
     * Processes an input with deadzone and exponential scaling.
     *
     * @param input the input to process
     * @return the processed input
     */
    private fun processInput(input: Double): Double {
        // Apply deadzone
        if (abs(input) < deadzone) return 0.0

        // Normalize input
        val normalizedInput = (input - deadzone) / (1 - deadzone)

        // Apply exponential scaling for fine control
        return normalizedInput.pow(inputExp) * if (input < 0) -1 else 1
    }

    var value: Double = 0.0

    fun update(input: Double) {
        value = processInput(input)
    }
}