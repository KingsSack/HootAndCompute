package dev.kingssack.volt.manual

import com.acmerobotics.roadrunner.PoseVelocity2d
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.robotcore.hardware.Gamepad
import org.firstinspires.ftc.robotcore.external.Telemetry

abstract class SimpleManualModeWithSpeedModes(
    telemetry: Telemetry,
    private val params: SimpleManualModeWithSpeedModesParams = SimpleManualModeWithSpeedModesParams(),
    manualParams: ManualParams = ManualParams()
) : ManualMode(telemetry, manualParams) {
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
        val turnScale: Double = 0.8,

        val turbo: Double = 1.0,
        val normal: Double = 0.5,
        val precise: Double = 0.2,
    )

    // Speed modes
    private val speedModes = mapOf(
        "TURBO" to params.turbo,
        "NORMAL" to params.normal,
        "PRECISE" to params.precise
    )
    private var currentSpeedMode = "NORMAL"

    private fun updateSpeedMode(gamepad: Gamepad) {
        when {
            gamepad.y -> {
                currentSpeedMode = "TURBO"
            }
            gamepad.b -> {
                currentSpeedMode = "NORMAL"
            }
            gamepad.a -> {
                currentSpeedMode = "PRECISE"
            }
        }
    }

    /**
     * Calculates the pose velocity of the robot based on the gamepad input.
     *
     * @param gamepad the gamepad input
     * @return the pose velocity
     */
    fun calculatePoseWithGamepad(gamepad: Gamepad): PoseVelocity2d {
        // Handle speed mode changes
        updateSpeedMode(gamepad)

        // Get gamepad input with deadzone and exponential scaling
        val x = processInput(-gamepad.left_stick_x.toDouble())
        val y = processInput(-gamepad.left_stick_y.toDouble())
        val rx = processInput(-gamepad.right_stick_x.toDouble()) * params.turnScale

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