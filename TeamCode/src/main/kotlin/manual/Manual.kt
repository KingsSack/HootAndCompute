package org.firstinspires.ftc.teamcode.manual

import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.PoseVelocity2d
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.attachment.Claw
import org.firstinspires.ftc.teamcode.attachment.Wrist
import org.firstinspires.ftc.teamcode.robot.Steve
import kotlin.math.abs
import kotlin.math.pow

/**
 * The Manual class implements the ManualMode interface and provides manual control of the robot.
 *
 * @param hardwareMap the hardware map
 * @param telemetry the telemetry
 * @param gamepad1 gamepad for movement
 * @param gamepad2 gamepad for actions
 *
 * @property controller the manual controller
 * @property robot the robot
 */
class Manual(
    hardwareMap: HardwareMap,
    telemetry: Telemetry,
    params: ManualParams,
    private val gamepad1: Gamepad,
    private val gamepad2: Gamepad
) : ManualMode {
    /**
     * ManualParams is a configuration object for manual control.
     *
     * @property deadzone the minimum joystick input to register
     * @property minPower the minimum power to move motors
     * @property turnScale the turn sensitivity
     * @property inputExp the input exponential for fine control
     * @property turbo the speed of the turbo speed mode
     * @property normal the speed of the normal speed mode
     * @property precise the speed of the precise speed mode
     * @property initialX the initial x position
     * @property initialY the initial y position
     * @property initialHeading the initial heading
     */
    class ManualParams {
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
        var normal: Double = 0.75
        @JvmField
        var precise: Double = 0.5

        @JvmField
        var initialX: Double = -66.0
        @JvmField
        var initialY: Double = 66.0
        @JvmField
        var initialHeading: Double = -90.0
    }

    override val controller = ManualController(telemetry)
    override val robot = Steve(hardwareMap, Pose2d(
        Vector2d(params.initialX, params.initialY),
        Math.toRadians(params.initialHeading)
    ))

    // Control parameters
    private val deadzone = params.deadzone
    private val minPower = params.minPower
    private val turnScale = params.turnScale
    private val inputExp = params.inputExp

    // Speed modes
    private val speedModes = mapOf(
        "TURBO" to params.turbo,
        "NORMAL" to params.normal,
        "PRECISE" to params.precise
    )
    private var currentSpeedMode = "NORMAL"

    override fun tick(telemetry: Telemetry) {
        // Drive
        driveWithGamepad(gamepad1)

        // Control lifters, claw, and extender
        controlLiftWithGamepad(gamepad2)
        controlClawWithGamepad(gamepad2)
        rotateClawWithGamepad(gamepad2)
        extendClawWithGamepad(gamepad2)

        // Run actions
        controller.runActions()

        // Update robot
        robot.update(telemetry)
    }

    private fun driveWithGamepad(gamepad: Gamepad) {
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

        // Set drive powers using robot.drive.setDrivePowers
        robot.drive.setDrivePowers(
            PoseVelocity2d(
                linearVelocity,
                angularVelocity
            )
        )
    }

    private fun processInput(input: Double): Double {
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

    private fun controlLiftWithGamepad(gamepad: Gamepad) {
        // Control lifters
        if (gamepad.right_bumper) controller.addAction(robot.lift.raise())
        else if (gamepad.left_bumper) controller.addAction(robot.lift.drop())
    }

    private fun controlClawWithGamepad(gamepad: Gamepad) {
        // Control claw
        robot.claw.setPower(Claw.maxPower * (gamepad.right_trigger - gamepad.left_trigger))
    }

    private fun rotateClawWithGamepad(gamepad: Gamepad) {
        // Control claw rotation
        if (gamepad.a) controller.addAction(robot.wrist.twistTo((Wrist.maxPosition - Wrist.minPosition) / 2))
        else if (gamepad.dpad_up) controller.addAction(robot.wrist.twistTo(robot.wrist.getPosition() + 0.05))
        else if (gamepad.dpad_down) controller.addAction(robot.wrist.twistTo(robot.wrist.getPosition() - 0.05))
    }

    private fun extendClawWithGamepad(gamepad: Gamepad) {
        // Control shoulder
        if (gamepad.x) controller.addAction(robot.shoulder.retract())
        else if (gamepad.y) controller.addAction(robot.shoulder.goTo(0))
        else if (gamepad.b) controller.addAction(robot.shoulder.extend())
    }
}