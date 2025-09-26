package dev.kingssack.volt.attachment

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

    protected var running = false

    /**
     * Represents an action that can be run on the attachment.
     */
    abstract inner class ControlAction : Action {
        private var initialized = false

        override fun run(p: TelemetryPacket): Boolean {
            if (!initialized) {
                init()
                initialized = true
                running = true
            }
            if (update(p)) {
                handleStop()
                running = false
                return false
            }
            return true
        }

        /**
         * Initializes the action.
         */
        abstract fun init()

        /**
         * Updates the action.
         *
         * @param packet the telemetry packet
         * @return whether the action is complete
         */
        abstract fun update(packet: TelemetryPacket): Boolean

        /**
         * Handles the end of the action.
         */
        abstract fun handleStop()
    }

    /**
     * Updates the attachment.
     *
     * @param telemetry for logging
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