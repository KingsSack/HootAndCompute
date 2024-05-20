package org.firstinspires.ftc.teamcode.attachments

import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.HardwareMap

class Launcher(hardwareMap: HardwareMap) {
    private lateinit var launcherMotor: CRServo

    fun init(hardwareMap: HardwareMap) {
        launcherMotor = hardwareMap.get(CRServo::class.java, "Launcher")
    }

    fun controller (gamepad: Gamepad) {
        launcherMotor.power = when {
            gamepad.right_trigger > 0.1 -> 0.72
            else -> 0.0
        }
    }

    init {
        init(hardwareMap)
    }
}
