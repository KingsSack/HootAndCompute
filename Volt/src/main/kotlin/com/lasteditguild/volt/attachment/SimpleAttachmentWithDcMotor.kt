package com.lasteditguild.volt.attachment

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * SimpleAttachmentWithDcMotor is an attachment that controls a motor.
 *
 * @param hardwareMap for registering the motor
 * @param name the name of the motor
 */
open class SimpleAttachmentWithDcMotor(hardwareMap: HardwareMap, private val name: String) : Attachment() {
    // Initialize motor
    protected val motor: DcMotor = hardwareMap.dcMotor[name]

    init {
        // Set motor direction
        motor.direction = DcMotorSimple.Direction.FORWARD

        // Set zero power behavior
        motor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        // Set motor mode
        motor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        motor.mode = DcMotor.RunMode.RUN_USING_ENCODER

        motors = listOf(motor)
    }

    /**
     * An action that extends or retracts the motor to a target position.
     *
     * @param power for the power of the motor
     * @param targetPosition for the target position of the motor
     */
    inner class SimpleAttachmentWithDcMotorControl(
        private val power: Double,
        private val targetPosition: Int,
        private val minPosition: Int,
        private val maxPosition: Int
    ) : ControlAction() {
        private var reversing = false

        override fun init() {
            // Check if the target position is valid
            if (targetPosition < minPosition || targetPosition > maxPosition)
                throw IllegalArgumentException("Target position is out of bounds")

            // Determine reversing
            reversing = targetPosition < motor.currentPosition

            // Set power
            motor.power = if (reversing) -power else power
        }

        override fun update(packet: TelemetryPacket): Boolean {
            // Get position
            val currentPosition = motor.currentPosition
            packet.put("DcMotor $name position", currentPosition)

            if (reversing) {
                // Lowering
                if (currentPosition > targetPosition)
                    return false
            } else {
                // Raising
                if (currentPosition < targetPosition)
                    return false
            }
            return true
        }

        override fun handleStop() {
            // Stop the motor
            motor.power = 0.0
        }
    }

    /**
     * Move the motor.
     *
     * @param power the power to set the motor to
     * @param position the target position of the motor
     * @param minPosition the lower limit of the motor
     * @param maxPosition the upper limit of the motor
     *
     * @return an action to move the motor to a position
     */
    fun goTo(power: Double, position: Int, minPosition: Int, maxPosition: Int): Action {
        return SimpleAttachmentWithDcMotorControl(power, position, minPosition, maxPosition)
    }

    /**
     * Set the power of the motor.
     *
     * @param power the power to set the motor to
     */
    fun setPower(power: Double) {
        // Set servo power
        motor.power = power
    }

    override fun update(telemetry: Telemetry) {
        telemetry.addData("Position", motor.currentPosition)
    }
}