package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * Represents an attachment to the robot.
 */
abstract class Attachment {
    protected var motors: List<DcMotor> = listOf()
    protected var servos: List<Servo> = listOf()
    protected var crServos: List<CRServo> = listOf()

    /**
     * Represents an action that can be run on the attachment.
     */
    abstract class ControlAction : Action {
        private var initialized = false

        override fun run(p: TelemetryPacket): Boolean {
            if (!initialized) {
                init()
                initialized = true
            }
            return update(p)
        }

        /**
         * Initializes the action.
         */
        abstract fun init()

        /**
         * Updates the action.
         *
         * @param packet the telemetry packet
         * @return whether the action is running
         */
        abstract fun update(packet: TelemetryPacket): Boolean
    }


    /**
     * Updates the attachment.
     *
     * @param telemetry For logging
     */
    abstract fun update(telemetry: Telemetry)

    /**
     * Stops the attachment.
     */
    fun stop() {
        motors.forEach { it.power = 0.0 }
        crServos.forEach { it.power = 0.0 }
    }
}