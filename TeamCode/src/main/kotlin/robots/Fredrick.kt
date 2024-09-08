package org.firstinspires.ftc.teamcode.robots

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.HardwareMap
import kotlin.math.abs
import kotlin.math.max

class Fredrick : Robot() {
    // Drive Motors
    private lateinit var leftFrontDrive : DcMotor
    private lateinit var rightFrontDrive : DcMotor
    private lateinit var leftRearDrive : DcMotor
    private lateinit var rightRearDrive : DcMotor

    // Initialize
    override fun init(hardwareMap: HardwareMap) {
        // Drive Motor init
        rightFrontDrive = hardwareMap.get(DcMotor::class.java, "MotorRF")
        leftFrontDrive = hardwareMap.get(DcMotor::class.java, "MotorLF")
        rightRearDrive = hardwareMap.get(DcMotor::class.java, "MotorRR")
        leftRearDrive = hardwareMap.get(DcMotor::class.java, "MotorLR")

        // Direction
        leftFrontDrive.direction = DcMotorSimple.Direction.REVERSE
        leftRearDrive.direction = DcMotorSimple.Direction.REVERSE
    }

    // Drive
    fun drive(gamepad: Gamepad) {
        // Drive
        val x : Float = gamepad.left_stick_x
        val y : Float = -gamepad.left_stick_y
        val rx : Float = gamepad.right_stick_x

        // Power
        var rfPower : Double = (y - x - rx).toDouble()
        var lfPower : Double = (y + x + rx).toDouble()
        var rrPower : Double = (y + x - rx).toDouble()
        var lrPower : Double = (y - x + rx).toDouble()

        // Normalize
        val maxPower : Float = max(1.toFloat(), (abs(x) + abs(y) + abs(rx)))
        rfPower /= maxPower
        lfPower /= maxPower
        rrPower /= maxPower
        lrPower /= maxPower

        // Slow mode
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
}