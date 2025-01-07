package com.lasteditguild.volt.manual

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.acmerobotics.roadrunner.PoseVelocity2d
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.robotcore.hardware.Gamepad
import org.firstinspires.ftc.robotcore.external.Telemetry
import kotlin.math.abs
import kotlin.math.pow

/**
 * ManualController is a class that manages the manual control of the robot.
 *
 * @param telemetry for logging
 */
@Config
class ManualController(
    private val telemetry: Telemetry
) {
    /**
     * Configuration object for manual control.
     *
     * @property deadzone the minimum joystick input to register
     * @property minPower the minimum power to move motors
     * @property turnScale the turn sensitivity
     * @property inputExp the input exponential for fine control
     * @property turbo the speed of the turbo speed mode
     * @property normal the speed of the normal speed mode
     * @property precise the speed of the precise speed mode
     */
    companion object Config {
        @JvmField
        var deadzone: Double = 0.05
        @JvmField
        var minPower: Double = 0.05
        @JvmField
        var turnScale: Double = 0.8
        @JvmField
        var inputExp: Double = 2.0

        @JvmField
        var turbo: Double = 1.0
        @JvmField
        var normal: Double = 0.6
        @JvmField
        var precise: Double = 0.2
    }

    private val dash: FtcDashboard? = FtcDashboard.getInstance()
    private var runningActions: MutableList<Action> = ArrayList()

    /**
     * Adds an action to the list of running actions.
     *
     * @param action the action to add
     */
    fun addAction(action: Action) {
        runningActions.add(action)
    }

    /**
     * Runs the actions in the list of running actions.
     */
    fun runActions() {
        val packet = TelemetryPacket()
        val newActions: MutableList<Action> = ArrayList()
        for (action in runningActions) {
            action.preview(packet.fieldOverlay())
            if (action.run(packet)) {
                newActions.add(action)
            }
        }
        runningActions = newActions
        dash?.sendTelemetryPacket(packet)
        telemetry.update()
    }

    // Speed modes
    private val speedModes = mapOf(
        "TURBO" to turbo,
        "NORMAL" to normal,
        "PRECISE" to precise
    )
    private var currentSpeedMode = "NORMAL"

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
        val rx = processInput(-gamepad.right_stick_x.toDouble()) * turnScale

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

    /**
     * Processes an input with deadzone and exponential scaling.
     *
     * @param input the input to process
     * @return the processed input
     */
    fun processInput(input: Double): Double {
        // Apply deadzone
        if (abs(input) < deadzone) return 0.0

        // Normalize input
        val normalizedInput = (input - deadzone) / (1 - deadzone)

        // Apply exponential scaling for fine control
        return normalizedInput.pow(inputExp) * if (input < 0) -1 else 1
    }

    private fun applyMinPower(power: Double): Double {
        return when {
            power > minPower -> power
            power < -minPower -> power
            else -> 0.0
        }
    }

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
}