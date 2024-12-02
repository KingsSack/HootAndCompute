package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.teamcode.Configuration

class Extender(hardwareMap: HardwareMap, name: String) : Attachment {
    // Constants
    private val minPosition = Configuration.extenderParams.minPosition
    private val maxPosition = Configuration.extenderParams.maxPosition

    // Initialize extender
    private val extenderServo = hardwareMap.get(Servo::class.java, name)

    // Position
    var currentPosition = extenderServo.position

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