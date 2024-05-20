package org.firstinspires.ftc.teamcode.attachments

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.HardwareMap

class Hook(hardwareMap: HardwareMap) {
    private lateinit var hookMotor: DcMotor

    fun init(hardwareMap: HardwareMap) {
        hookMotor = hardwareMap.get(DcMotor::class.java, "Hook")
    }

    fun controller(gamepad: Gamepad) {
        hookMotor.power = when {
            gamepad.dpad_up -> 1.0
            gamepad.dpad_down -> -1.0
            else -> 0.0
        }

        /* if (gamepad.y) {
            hook.power = 0.8
        } else if (gamepad.a) {
            hook.power = -0.8
        } else {
            hook.power = 0.0
        } */
    }

    init {
        init(hardwareMap)
    }
}