package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.attachment.SimpleAttachmentWithServo
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * Tail is an attachment that moves the Intake.
 *
 * @param hardwareMap for initializing the servo
 * @param name the name of the tail servo
 *
 * @see Intake
 */
@Config
class Tail(
    hardwareMap: HardwareMap,
    name: String
): SimpleAttachmentWithServo(
    hardwareMap,
    name
) {
    /**
     * Params is a companion object that holds the configuration for the tail attachment.
     *
     * @property minPosition the minimum position of the tail
     * @property centerPosition the center position of the tail
     * @property maxPosition the maximum position of the tail
     */
    companion object Params {
        @JvmField
        var minPosition: Double = 0.0
        @JvmField
        var centerPosition: Double = 0.47
        @JvmField
        var maxPosition: Double = 0.486
    }

    fun extend(): Action {
        return SimpleAttachmentWithServoControl(maxPosition)
    }
    fun center(): Action {
        return SimpleAttachmentWithServoControl(centerPosition)
    }
    fun retract(): Action {
        return SimpleAttachmentWithServoControl(minPosition)
    }

    override fun update(telemetry: Telemetry) {
        telemetry.addLine("==== TAIL ====")
        super.update(telemetry)
        telemetry.addLine()
    }
}