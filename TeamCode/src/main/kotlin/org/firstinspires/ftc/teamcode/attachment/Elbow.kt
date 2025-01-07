package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.lasteditguild.volt.attachment.Attachment
import com.lasteditguild.volt.attachment.SimpleAttachmentWithCRServo
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.util.ElapsedTime

/**
 * Elbow is an attachment that bends the Claw.
 *
 * @param hardwareMap for initializing the servo
 * @param name the name of the elbow servo
 *
 * @see Claw
 */
@Config
class Elbow(hardwareMap: HardwareMap, name: String) : SimpleAttachmentWithCRServo(hardwareMap, name) {
    /**
     * Params is a companion object that holds the configuration for the elbow attachment.
     *
     * @property maxPower the maximum power of the elbow
     * @property timeForFullExtend the time it takes to fully extend the elbow
     */
    companion object Params {
        @JvmField
        var maxPower: Double = 0.72
        @JvmField
        var timeForFullExtend: Double = 1.0
    }

    // Actions
    fun extend(): Action {
        return SimpleAttachmentWithCRServoControl(-maxPower, timeForFullExtend)
    }
    fun retract(): Action {
        return SimpleAttachmentWithCRServoControl(maxPower, timeForFullExtend)
    }
    fun moveFor(seconds: Double): Action {
        return SimpleAttachmentWithCRServoControl(maxPower, seconds)
    }
}