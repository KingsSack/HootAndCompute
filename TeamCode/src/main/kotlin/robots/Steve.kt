package org.firstinspires.ftc.teamcode.robots

import com.qualcomm.hardware.dfrobot.HuskyLens
import com.qualcomm.robotcore.hardware.*
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import kotlin.math.abs
import kotlin.math.max

class Steve : Robot() {
    // Drive motors
    private lateinit var leftFrontDrive : DcMotor
    private lateinit var rightFrontDrive : DcMotor
    private lateinit var leftRearDrive : DcMotor
    private lateinit var rightRearDrive : DcMotor

    // Sensors
    private lateinit var imu : IMU
    private lateinit var distanceSensor : DistanceSensor
    private lateinit var huskyLens : HuskyLens

    override fun init(hardwareMap: HardwareMap) {
        // Register hardware
        registerMotors(hardwareMap)
        registerSensors(hardwareMap)

        // Reset IMU
        imu.resetYaw()

        // Set huskylens mode
        huskyLens.selectAlgorithm(HuskyLens.Algorithm.OBJECT_RECOGNITION)
    }

    override fun manualControl(gamepad: Gamepad) {
        // Get gamepad input
        val x : Float = gamepad.left_stick_x
        val y : Float = -gamepad.left_stick_y
        val rx : Float = gamepad.right_stick_x

        // Calculate power
        var rfPower : Double = (y - x - rx).toDouble()
        var lfPower : Double = (y + x + rx).toDouble()
        var rrPower : Double = (y + x - rx).toDouble()
        var lrPower : Double = (y - x + rx).toDouble()

        // Normalize power
        val maxPower : Float = max(1.toFloat(), (abs(x) + abs(y) + abs(rx)))
        rfPower /= maxPower
        lfPower /= maxPower
        rrPower /= maxPower
        lrPower /= maxPower

        // Slow mode logic
        if (gamepad.right_bumper) {
            rfPower *= 0.5
            lfPower *= 0.5
            rrPower *= 0.5
            lrPower *= 0.5
        }

        // Set power
        rightFrontDrive.power = rfPower
        leftFrontDrive.power = lfPower
        rightRearDrive.power = rrPower
        leftRearDrive.power = lrPower
    }

    override fun halt() {
        // Stop all drive motors
        leftFrontDrive.power = 0.0
        rightFrontDrive.power = 0.0
        leftRearDrive.power = 0.0
        rightRearDrive.power = 0.0
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