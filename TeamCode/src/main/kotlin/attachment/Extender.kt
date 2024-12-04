package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.teamcode.Configuration

/**
 * Extender is an attachment that can extend and retract.
 *
 * @param hardwareMap the hardware map
 * @param name the name of the extender servo
 */
class Extender(hardwareMap: HardwareMap, name: String) : Attachment {
    // Constants
    private val minPosition = Configuration.extenderParams.minPosition
    private val maxPosition = Configuration.extenderParams.maxPosition

    // Initialize extender
    private val extenderServo = hardwareMap.get(Servo::class.java, name)

    // Position
    var currentPosition = extenderServo.position

    /**
     * Control is an action that extends or retracts the extender.
     *
     * Action occurs instantly.
     *
     * @param servo the servo that controls the extender
     * @param targetPosition the position to set the servo to
     */
    private class Control(private val servo: Servo, private val targetPosition: Double) : Action {
        override fun run(p: TelemetryPacket): Boolean {
            servo.position = targetPosition
            return false
        }
    }
    fun extend() : Action {
        return Control(extenderServo, minPosition)
    }
    fun retract() : Action {
        return Control(extenderServo, maxPosition)
    }
}