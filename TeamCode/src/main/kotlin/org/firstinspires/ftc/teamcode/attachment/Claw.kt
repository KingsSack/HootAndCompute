package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.Action
import dev.kingssack.volt.attachment.SimpleAttachmentWithServo
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * Claw is an attachment that can open and close.
 *
 * @param hardwareMap the hardware map
 * @param name the name of the claw servo
 */
@Config
class Claw(
    hardwareMap: HardwareMap,
    name: String
) : SimpleAttachmentWithServo(
    hardwareMap,
    name
) {
    /**
     * Params is a companion object that holds the configuration for the claw attachment.
     *
     * @property minPosition the minimum position of the claw
     * @property maxPosition the maximum position of the claw
     */
    companion object Params {
        @JvmField
        var minPosition: Double = 0.5
        @JvmField
        var maxPosition: Double = 1.0
    }

    fun open(): Action {
        return SimpleAttachmentWithServoControl(minPosition)
    }
    fun close(): Action {
        return SimpleAttachmentWithServoControl(maxPosition)
    }

    override fun update(telemetry: Telemetry) {
        telemetry.addLine("==== CLAW ====")
        super.update(telemetry)
        telemetry.addLine()
    }
}