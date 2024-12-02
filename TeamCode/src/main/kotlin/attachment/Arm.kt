package org.firstinspires.ftc.teamcode.attachment

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.HardwareMap

class Arm(hardwareMap: HardwareMap, name: String) : Attachment {
    // Initialize motor
    private var liftMotor = hardwareMap.get(DcMotor::class.java, name)

    fun lift(power: Double) {
        // Move arm
        liftMotor.power = power
    }
}