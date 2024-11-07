package robot

import attachment.Claw
import com.qualcomm.hardware.dfrobot.HuskyLens
import com.qualcomm.robotcore.hardware.*
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import kotlin.math.abs
import kotlin.math.pow

class Steve : Robot() {
    // Drive motors
    private lateinit var leftFrontDrive: DcMotor
    private lateinit var rightFrontDrive: DcMotor
    private lateinit var leftRearDrive: DcMotor
    private lateinit var rightRearDrive: DcMotor

    // Sensors
    private lateinit var imu: IMU
    private lateinit var distanceSensor: DistanceSensor
    private lateinit var huskyLens: HuskyLens

    // Attachments
    private lateinit var claw : Claw

    // Control parameters
    private var deadzone = 0.05  // Minimum stick movement to register
    private var minPower = 0.05  // Minimum power to move motors
    private var turnScale = 0.8  // Reduce turn sensitivity
    private var inputExp = 2.0   // Input exponential for fine control

    // Speed modes
    private val speedModes = mapOf(
        "TURBO" to 1.0,
        "NORMAL" to 0.8,
        "PRECISE" to 0.4
    )
    private var currentSpeedMode = "NORMAL"

    override fun init(hardwareMap: HardwareMap) {
        // Register hardware
        registerMotors(hardwareMap)
        registerSensors(hardwareMap)
        registerAttachments(hardwareMap)

        // Configure motors for better control
        configureDriveMotors()

        // Reset IMU
        imu.resetYaw()

        // Set huskylens mode
        huskyLens.selectAlgorithm(HuskyLens.Algorithm.OBJECT_RECOGNITION)
    }

    private fun configureDriveMotors() {
        // Configure all drive motors
        listOf(leftFrontDrive, rightFrontDrive, leftRearDrive, rightRearDrive).forEach { motor ->
            motor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE  // Better stopping
            motor.mode = DcMotor.RunMode.RUN_USING_ENCODER  // Enable encoder feedback
        }
    }

    override fun driveWithGamepad(gamepad: Gamepad) {
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

        // Set motor powers with minimum power threshold
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
            gamepad.y -> currentSpeedMode = "TURBO"
            gamepad.b -> currentSpeedMode = "NORMAL"
            gamepad.a -> currentSpeedMode = "PRECISE"
        }
    }

    override fun halt() {
        // Stop all drive motors
        setMotorPowers(listOf(0.0, 0.0, 0.0, 0.0))
    }

    fun controlClawWithGamepad(gamepad: Gamepad) {
        // Control arm
        if (gamepad.a) claw.setPower(claw.maxPower)
        else if (gamepad.b) claw.setPower(-claw.maxPower)
        else claw.setPower(0.0)
    }

    private fun registerMotors(hardwareMap: HardwareMap) {
        // Register motors
        leftFrontDrive = hardwareMap.get(DcMotor::class.java, "lf")
        rightFrontDrive = hardwareMap.get(DcMotor::class.java, "rf")
        leftRearDrive = hardwareMap.get(DcMotor::class.java, "lr")
        rightRearDrive = hardwareMap.get(DcMotor::class.java, "rr")

        // Set motor directions
        leftFrontDrive.direction = DcMotorSimple.Direction.FORWARD
        rightFrontDrive.direction = DcMotorSimple.Direction.REVERSE
        leftRearDrive.direction = DcMotorSimple.Direction.FORWARD
        rightRearDrive.direction = DcMotorSimple.Direction.REVERSE
    }

    private fun registerAttachments(hardwareMap: HardwareMap) {
        // Register attachments
        claw = Claw(hardwareMap, "claw")
    }

    private fun registerSensors(hardwareMap: HardwareMap) {
        // Register sensors
        imu = hardwareMap.get(IMU::class.java, "imu")
        distanceSensor = hardwareMap.get(DistanceSensor::class.java, "lidar")
        huskyLens = hardwareMap.get(HuskyLens::class.java, "lens")
    }

    fun getDetectedObjects(telemetry: Telemetry): Array<out HuskyLens.Block>? {
        // Get objects
        val blocks = huskyLens.blocks()
        telemetry.addData("Block count", blocks.size)
        for (block in blocks) {
            telemetry.addData("Block", block.toString())
        }
        return blocks
    }

    fun getDistanceToObstacle(telemetry: Telemetry): Double {
        // Get distance
        val distance = distanceSensor.getDistance(DistanceUnit.CM)
        telemetry.addData("range", "%.01f cm".format(distance))
        return distance
    }
}