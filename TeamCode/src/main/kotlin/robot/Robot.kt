package org.firstinspires.ftc.teamcode.robot

import com.qualcomm.robotcore.hardware.HardwareMap

interface Robot {
    // Register
    fun registerSensors(hardwareMap: HardwareMap)
    fun registerAttachments(hardwareMap: HardwareMap)
}