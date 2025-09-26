package dev.kingssack.volt.attachment

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * SimpleAttachmentWithServo is an attachment that controls a servo.
 *
 * @param hardwareMap for registering the servo
 * @param name the name of the servo
 */
open class SimpleAttachmentWithServo(hardwareMap: HardwareMap, private val name: String) : Attachment() {
    // Initialize servo
    protected val servo: Servo = hardwareMap.servo[name]

    init {
        servos = listOf(servo)
    }

    /**
     * An action that moves a servo to a [targetPosition].
     */
    inner class SimpleAttachmentWithServoControl(
        private val targetPosition: Double
    ) : ControlAction() {
        override fun init() {
            servo.position = targetPosition
        }

        override fun update(packet: TelemetryPacket): Boolean {
            return true
        }

        override fun handleStop() {
            // Do nothing
        }
    }

    /**
     * Go to a specified [position].
     *
     * @return an action to move the servo to a position
     */
    fun goTo(position: Double): Action {
        return SimpleAttachmentWithServoControl(position)
    }

    /**
     * Get the position of the servo.
     *
     * @return the position of the servo
     */
    fun getPosition(): Double {
        return servo.position
    }

    override fun update(telemetry: Telemetry) {
        telemetry.addData("Position", servo.position)
    }
}