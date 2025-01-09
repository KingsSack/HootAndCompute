package com.lasteditguild.volt.attachment

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * Claw is an attachment that can open and close.
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
     * An action that moves a servo to a target position.
     *
     * @param targetPosition the position to set the servo to
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
     * Go to a position.
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