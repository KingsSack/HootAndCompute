package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.lasteditguild.volt.attachment.Attachment
import com.lasteditguild.volt.attachment.CRServoWithPotentiometer
import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * Elbow is an attachment that bends the Claw.
 *
 * @param hardwareMap for initializing the servo
 * @param name the name of the elbow servo
 *
 * @see Claw
 */
@Config
class Elbow(hardwareMap: HardwareMap, name: String, private val potentiometer: AnalogInput) : CRServoWithPotentiometer(hardwareMap, name, potentiometer, false) {
    /**
     * Params is a companion object that holds the configuration for the elbow attachment.
     *
     * @property maxPower the maximum power of the elbow
     * @property extendedAngle the max angle
     * @property retractedAngle the min angle
     */
    companion object Params {
        @JvmField
        var maxPower: Double = 1.0
        @JvmField
        var extendedAngle: Double = 0.33
        @JvmField
        var retractedAngle: Double = 0.75
    }

    // Actions
    fun extend(): Action {
        return CRServoWithPotentiometer(-maxPower, extendedAngle)
    }
    fun retract(): Action {
        return CRServoWithPotentiometer(maxPower, retractedAngle)
    }

    override fun update(telemetry: Telemetry) {
        telemetry.addLine("==== ELBOW ====")
        super.update(telemetry)
        telemetry.addData("Position", potentiometer.voltage / 3.3)
        telemetry.addLine()
    }
}