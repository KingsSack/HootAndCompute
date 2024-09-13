package org.firstinspires.ftc.teamcode.attachments

import com.qualcomm.robotcore.hardware.HardwareMap

abstract class Attachment {
    // Initialize attachment
    abstract fun init(hardwareMap: HardwareMap)
}