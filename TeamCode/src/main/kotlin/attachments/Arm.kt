package org.firstinspires.ftc.teamcode.attachments

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.util.ElapsedTime

class Arm(hardwareMap: HardwareMap) {
    private lateinit var armMotor: DcMotor

    fun init(hardwareMap: HardwareMap) {
        armMotor = hardwareMap.get(DcMotor::class.java, "Arm")
        armMotor.mode = DcMotor.RunMode.RUN_USING_ENCODER
    }

    fun controller(gamepad: Gamepad, runtime: ElapsedTime) {
        if (gamepad.b) {
            if (gamepad.right_bumper) {
                lift(570, 0.5, runtime)
            }
            else {
                lift(425, 0.6, runtime)
            }
        }
        else if (gamepad.x) {
            lift(0, 0.5, runtime)
        }
    }

    private fun lift(target: Int, power: Double, runtime: ElapsedTime) {
        armMotor.targetPosition = target
        armMotor.mode = DcMotor.RunMode.RUN_TO_POSITION

        runtime.reset()
        armMotor.power = power

        while (armMotor.isBusy) {
            // Do nothing
        }

        armMotor.power = 0.0
        armMotor.mode = DcMotor.RunMode.RUN_USING_ENCODER
    }

    init {
        init(hardwareMap)
    }
}