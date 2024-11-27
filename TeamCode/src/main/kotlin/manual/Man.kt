package org.firstinspires.ftc.teamcode.manual

import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.robot.Steve
import kotlin.math.abs
import kotlin.math.pow

class Man(
    hardwareMap: HardwareMap,
    private val gamepad1: Gamepad,
    private val gamepad2: Gamepad
) : Manual {
    // Robot
    private val robot = Steve(hardwareMap)

    // Drive motors
    private lateinit var leftFrontDrive: DcMotorEx
    private lateinit var rightFrontDrive: DcMotorEx
    private lateinit var leftRearDrive: DcMotorEx
    private lateinit var rightRearDrive: DcMotorEx

    // Control parameters
    private val deadzone = 0.05 // Minimum stick movement to register
    private val minPower = 0.05 // Minimum power to move motors
    private val turnScale = 0.8 // Reduce turn sensitivity
    private val inputExp = 2.0  // Input exponential for fine control

    // Speed modes
    private val speedModes = mapOf(
        "TURBO" to 1.0,
        "NORMAL" to 0.8,
        "PRECISE" to 0.4
    )
    private var currentSpeedMode = "NORMAL"

    init {
        // Register motors
        registerMotors(hardwareMap)
    }

    override fun registerMotors(hardwareMap: HardwareMap) {
        // Register motors
        leftFrontDrive = hardwareMap.get(DcMotorEx::class.java, "lf")
        rightFrontDrive = hardwareMap.get(DcMotorEx::class.java, "rf")
        leftRearDrive = hardwareMap.get(DcMotorEx::class.java, "lr")
        rightRearDrive = hardwareMap.get(DcMotorEx::class.java, "rr")

        // Set motor directions
        leftFrontDrive.direction = DcMotorSimple.Direction.FORWARD
        rightFrontDrive.direction = DcMotorSimple.Direction.REVERSE
        leftRearDrive.direction = DcMotorSimple.Direction.FORWARD
        rightRearDrive.direction = DcMotorSimple.Direction.REVERSE
    }

    override fun tick(telemetry: Telemetry) {
        // Drive
        driveWithGamepad(gamepad1)

        // Control lifters, claw, and extender
        controlLiftersWithGamepad(gamepad2, telemetry)
        controlClawWithGamepad(gamepad2)
        controlExtenderWithGamepad(gamepad2, telemetry)

        // Tick robot
        robot.tick()

        // Update telemetry
        telemetry.update()
    }

    private fun driveWithGamepad(gamepad: Gamepad) {
        // Handle speed mode changes
        updateSpeedMode(gamepad)

        // Get gamepad input with deadzone and exponential scaling
        val x = processInput(gamepad.left_stick_x.toDouble())
        val y = processInput(-gamepad.left_stick_y.toDouble())  // Inverted Y
        val rx = processInput(gamepad.right_stick_x.toDouble()) * turnScale

        // Calculate mecanum drive powers
        val powers = calculateMecanumPowers(x, y, rx)

        // Apply current speed mode scaling
        val scaledPowers = powers.map { it * speedModes[currentSpeedMode]!! }

        // Set motor powers with a minimum power threshold
        setMotorPowers(scaledPowers)
    }

    private fun processInput(input: Double): Double {
        // Apply deadzone
        if (abs(input) < deadzone) return 0.0

        // Normalize input
        val normalizedInput = (input - deadzone) / (1 - deadzone)

        // Apply exponential scaling for fine control
        return normalizedInput.pow(inputExp) * if (input < 0) -1 else 1
    }

    private fun calculateMecanumPowers(x: Double, y: Double, rx: Double): List<Double> {
        // Calculate raw powers
        val rfPower = y - x - rx
        val lfPower = y + x + rx
        val rrPower = y + x - rx
        val lrPower = y - x + rx

        // Find maximum magnitude
        val maxMagnitude = maxOf(abs(rfPower), abs(lfPower), abs(rrPower), abs(lrPower))

        // Normalize powers
        val normalizationFactor = if (maxMagnitude > 1.0) maxMagnitude else 1.0
        return listOf(rfPower, lfPower, rrPower, lrPower).map { it / normalizationFactor }
    }

    private fun setMotorPowers(powers: List<Double>) {
        // Apply minimum power threshold and set motors
        rightFrontDrive.power = applyMinPower(powers[0])
        leftFrontDrive.power = applyMinPower(powers[1])
        rightRearDrive.power = applyMinPower(powers[2])
        leftRearDrive.power = applyMinPower(powers[3])
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

    private fun controlLiftersWithGamepad(gamepad: Gamepad, telemetry: Telemetry) {
        // Control lifters
        if (gamepad.right_bumper) {
            robot.control.addAction(robot.lift.raise())
        }
        else if (gamepad.left_bumper) {
            robot.control.addAction(robot.lift.drop())
        }
    }

    private fun controlClawWithGamepad(gamepad: Gamepad) {
        // Control arm
        if (gamepad.a) robot.claw.setPower(robot.claw.maxPower)
        else if (gamepad.b) robot.claw.setPower(-robot.claw.maxPower)
        else robot.claw.setPower(0.0)
    }

    private fun controlExtenderWithGamepad(gamepad: Gamepad, telemetry: Telemetry) {
        // Control extender
        val position = robot.extender.currentPosition
        telemetry.addData("Extender position", "%5.2f", position)
        if (gamepad.x) {
            if (position == 0.0) robot.control.addAction(robot.extender.extend())
            else robot.control.addAction(robot.extender.retract())
        }
        if (gamepad.y) {
            // extender.setPos(0.4)
        }
    }
}