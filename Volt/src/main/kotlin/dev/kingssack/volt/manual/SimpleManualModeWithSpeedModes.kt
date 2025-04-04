package dev.kingssack.volt.manual

import com.acmerobotics.roadrunner.PoseVelocity2d
import com.acmerobotics.roadrunner.Vector2d
import dev.kingssack.volt.robot.Robot

/**
 * SimpleManualModeWithSpeedModes is an abstract class
 * that defines the methods for running a manual mode with speed modes.
 *
 * @property params the configuration object for manual control
 */
abstract class SimpleManualModeWithSpeedModes<R : Robot>(
    private val params: SimpleManualModeWithSpeedModesParams = SimpleManualModeWithSpeedModesParams(),
    manualParams: ManualMode.ManualParams = ManualMode.ManualParams()
) : ManualMode<R>(manualParams) {
    /**
     * Configuration object for manual control.
     *
     * @property minPower the minimum power to register
     * @property turnScale the turn sensitivity
     * @property turbo the speed of the turbo speed mode
     * @property normal the speed of the normal speed mode
     * @property precise the speed of the precise speed mode
     */
    class SimpleManualModeWithSpeedModesParams (
        val minPower: Double = 0.05,
        val turnScale: Double = 0.9,

        val turbo: Double = 1.0,
        val normal: Double = 0.5,
        val precise: Double = 0.2,
        val slow: Double = 0.1,
    )

    // Speed modes
    private val speedModes = mapOf(
        "TURBO" to params.turbo,
        "NORMAL" to params.normal,
        "PRECISE" to params.precise,
        "SLOW" to params.slow
    )
    private var currentSpeedMode = "NORMAL"

    private fun updateSpeedMode() {
        when {
            isButtonTapped("y1") -> {
                currentSpeedMode = "TURBO"
            }
            isButtonTapped("b1") -> {
                currentSpeedMode = "NORMAL"
            }
            isButtonTapped("a1") -> {
                currentSpeedMode = "PRECISE"
            }
            isButtonTapped("x1") -> {
                currentSpeedMode = "SLOW"
            }
        }
    }

    /**
     * Calculates the pose velocity of the robot based on the gamepad input.
     *
     * @return the pose velocity
     */
    fun calculatePoseWithGamepad(): PoseVelocity2d {
        // Handle speed mode changes
        updateSpeedMode()

        // Get gamepad input with deadzone and exponential scaling
        val x = -getAnalogValue("left_stick_x1")
        val y = -getAnalogValue("left_stick_y1")
        val rx = -getAnalogValue("right_stick_x1") * params.turnScale

        // Apply current speed mode scaling
        val scale = speedModes[currentSpeedMode]!!

        // Create linear and angular velocities with scaling
        val linearVelocity = Vector2d(
            applyMinPower(y * scale),
            applyMinPower(x * scale)
        )
        val angularVelocity = applyMinPower(rx * scale)

        // Return the calculated pose velocity
        return PoseVelocity2d(
            linearVelocity,
            angularVelocity
        )
    }

    private fun applyMinPower(power: Double): Double {
        return when {
            power > params.minPower -> power
            power < -params.minPower -> power
            else -> 0.0
        }
    }
}