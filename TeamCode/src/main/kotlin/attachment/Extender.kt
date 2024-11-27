package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.hardware.HardwareMap

class Extender(hardwareMap: HardwareMap, name: String) : Attachment {
    // Constants
    private val minPosition = 0.0
    private val maxPosition = 1.0

    // Initialize extender
    private val extenderServo = hardwareMap.get(Servo::class.java, name)

    // Position
    var currentPosition = extenderServo.position

    private class Extend(private val servo: Servo, private val targetPosition: Double) : Action {
        override fun run(p: TelemetryPacket): Boolean {
            servo.position = targetPosition
            return false
        }
    }
    fun extend() : Action {
        return Extend(extenderServo, minPosition)
    }
    fun retract() : Action {
        return Extend(extenderServo, maxPosition)
    }
}