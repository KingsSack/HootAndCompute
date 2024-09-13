package org.firstinspires.ftc.teamcode.robots

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.HardwareMap
import kotlin.math.abs
import kotlin.math.max

class Goose : Robot() {
    // Drive motors
    private lateinit var testMotor : DcMotor

    override fun init(hardwareMap: HardwareMap) {
        testMotor = hardwareMap.get(DcMotor::class.java, "Test Motor")
    }

    override fun manualControl(gamepad: Gamepad) {
        // Get gamepad input
        val x : Float = gamepad.left_stick_x
        val y : Float = -gamepad.left_stick_y
        val rx : Float = gamepad.right_stick_x

        // Calculate power
        var testPower : Double = (y - x - rx).toDouble()

        // Normalize power
        val maxPower : Float = max(1.toFloat(), (abs(x) + abs(y) + abs(rx)))
        testPower /= maxPower

        // Set power
        testMotor.power = testPower
    }

    override fun halt() {
        // Stop all drive motors
        testMotor.power = 0.0
    }
}