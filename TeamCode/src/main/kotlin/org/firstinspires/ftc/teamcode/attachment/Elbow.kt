package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.Action
import dev.kingssack.volt.attachment.CRServoWithPotentiometer
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
class Elbow(
    hardwareMap: HardwareMap,
    name: String,
    private val potentiometer: AnalogInput
) : CRServoWithPotentiometer(
    hardwareMap,
    name,
    potentiometer,
    servoReversed
) {
    /**
     * Params is a companion object that holds the configuration for the elbow attachment.
     *
     * @property maxPower the maximum power of the elbow
     * @property extendedPosition the extended position of the elbow
     * @property retractedPosition the retracted position of the elbow
     */
    companion object Params {
        @JvmField
        var servoReversed: Boolean = true
        @JvmField
        var maxPower: Double = 1.0
        @JvmField
        var extendedPosition: Double = 1.0
        @JvmField
        var retractedPosition: Double = 3.0
    }

    // Actions
    fun extend(): Action {
        return CRServoWithPotentiometer(-maxPower, extendedPosition)
    }
    fun retract(): Action {
        return CRServoWithPotentiometer(maxPower, retractedPosition)
    }

    override fun update(telemetry: Telemetry) {
        telemetry.addLine("==== ELBOW ====")
        super.update(telemetry)
        telemetry.addData("Position", potentiometer.voltage / 3.3)
        telemetry.addLine()
    }
}