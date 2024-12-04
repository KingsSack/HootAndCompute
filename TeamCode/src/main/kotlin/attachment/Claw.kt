package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.teamcode.Configuration

/**
 * Claw is an attachment that can open and close.
 *
 * @param hardwareMap the hardware map
 * @param name the name of the claw servo
 *
 * @property maxPower the maximum power of the claw
 */
class Claw(hardwareMap: HardwareMap, name: String) : Attachment {
    // Constants
    val maxPower = Configuration.clawParams.maxPower
    private val openCloseTime = Configuration.clawParams.openCloseTime

    // Initialize claw
    private var clawServo: CRServo = hardwareMap.get(CRServo::class.java, name)

    /**
     * Control is an action that opens or closes the claw.
     *
     * @param servo the servo that controls the claw
     * @param power the power to set the servo to
     * @param time the time to set the servo to the power
     */
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

    /**
     * Set the power of the claw.
     *
     * @param power the power to set the claw to
     */
    fun setPower(power: Double) {
        // Set servo power
        clawServo.power = power
    }
}