package org.firstinspires.ftc.teamcode.robots

import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.HardwareMap

abstract class Robot {
    // Initialize
    abstract fun init(hardwareMap: HardwareMap)

    // Drive
    abstract fun manualControl(gamepad: Gamepad)

    // Stop
    abstract fun halt()
}