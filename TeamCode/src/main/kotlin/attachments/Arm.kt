package org.firstinspires.ftc.teamcode.attachments

import Attachment
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.HardwareMap

class Arm(hardwareMap: HardwareMap) : Attachment(hardwareMap) {
    // Motors
    private lateinit var liftMotor : DcMotor

    override fun init(hardwareMap: HardwareMap) {
        // Initialize arm
        liftMotor = hardwareMap.get(DcMotor::class.java, "lm")
    }

    fun liftArm(power: Double) {
        // Move arm
        liftMotor.power = power
    }
}