package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import dev.kingssack.volt.attachment.Attachment

/**
 * [Launcher] is an [Attachment] that controls a dual motor flywheel launcher.
 *
 * @param leftMotor the left [DcMotor] of the [Launcher]
 * @param rightMotor the right [DcMotor] of the [Launcher]
 */
class Launcher(private val leftMotor: DcMotor, private val rightMotor: DcMotor) :
    Attachment("Launcher") {
    init {
        leftMotor.direction = DcMotorSimple.Direction.FORWARD
        rightMotor.direction = DcMotorSimple.Direction.REVERSE

        leftMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        rightMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
    }

    fun enable(): Action = action {
        init {
            leftMotor.power = 1.0
            rightMotor.power = 1.0
        }

        loop { true }
    }

    fun disable(): Action = action {
        init {
            leftMotor.power = 0.0
            rightMotor.power = 0.0
        }

        loop { true }
    }

    fun setPower(power: Double): Action = action {
        init {
            leftMotor.power = power
            rightMotor.power = power
        }

        loop { true }
    }
}
