package dev.kingssack.volt.attachment

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
open class SimpleAttachmentWithServo(hardwareMap: HardwareMap, private val name: String) :
    Attachment() {
    // Initialize servo
    protected val servo: Servo = hardwareMap.servo[name]

    /**
     * Go to a specified [position].
     *
     * @return an action to move the servo to a position
     */
    fun goTo(position: Double): Action =
        controlAction(init = { servo.position = position }, update = { true })

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