package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.util.ElapsedTime

class Claw(hardwareMap: HardwareMap, name: String) : Attachment {
    // Constants
    val maxPower = 0.72
    private val openCloseTime = 0.6

    // Initialize claw
    private var clawServo: CRServo = hardwareMap.get(CRServo::class.java, name)

    private class Control(private val servo: CRServo, private val power: Double, private val time: Double) : Action {
        private var initialized = false

        // Runtime
        private val runtime: ElapsedTime = ElapsedTime()

        override fun run(p: TelemetryPacket): Boolean {
            if (!initialized) {
                // Set power
                runtime.reset()
                servo.power = power
                initialized = true
            }

            if (runtime.seconds() < time)
                return true

            // Stop servo
            servo.power = 0.0
            return false
        }
    }
    fun open() : Action {
        return Control(clawServo, maxPower, openCloseTime)
    }
    fun close() : Action {
        return Control(clawServo, -maxPower, openCloseTime)
    }


    fun setPower(power: Double) {
        // Set servo power
        clawServo.power = power
    }
}