package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.config.Config
import com.lasteditguild.volt.attachment.SimpleAttachmentWithServo
import com.qualcomm.robotcore.hardware.HardwareMap

/**
 * Wrist is an attachment that twists the Claw.
 *
 * @param hardwareMap for initializing the servo
 * @param name the name of the wrist servo
 *
 * @see Claw
 */
@Config
class Wrist(hardwareMap: HardwareMap, name: String) : SimpleAttachmentWithServo(hardwareMap, name) {
    /**
     * Params is a companion object that holds the configuration for the wrist attachment.
     *
     * @property minPosition the minimum position of the wrist
     * @property maxPosition the maximum position of the wrist
     */
    companion object Params {
        @JvmField
        var minPosition: Double = 0.0
        @JvmField
        var maxPosition: Double = 1.0
    }
}