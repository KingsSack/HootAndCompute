package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * Shoulder is an attachment that extends and retracts the Claw.
 *
 * @param hardwareMap for initializing motor
 * @param name the name of the shoulder motor
 *
 * @see Claw
 */
@Config
class Shoulder(hardwareMap: HardwareMap, name: String) : Attachment() {
    /**
     * Params is a companion object that holds the configuration for the shoulder attachment.
     *
     * @property maxPosition the maximum position of the shoulder
     * @property minPosition the minimum position of the shoulder
     * @property maxPower the maximum power of the shoulder
     */
    companion object Params {
        @JvmField
        var maxPosition: Int = 100
        @JvmField
        var minPosition: Int = 15
        @JvmField
        var maxPower: Double = 0.6
    }

    // Initialize shoulder motor
    private val shoulder = hardwareMap.dcMotor[name]

    init {
        // Set motor direction
        shoulder.direction = DcMotorSimple.Direction.REVERSE

        // Set zero power behavior
        shoulder.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        // Set motor mode
        shoulder.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        shoulder.mode = DcMotor.RunMode.RUN_USING_ENCODER

        motors = listOf(shoulder)
    }

    /**
     * Control is an action that extends or retracts the shoulder to a target position.
     *
     * @param power for the power of the shoulder
     * @param targetPosition for the target position of the shoulder
     */
    inner class Control(
        private val power: Double,
        private val targetPosition: Int
    ) : ControlAction() {
        private var lowering = false

        override fun init() {
            // Check if the target position is valid
            if (targetPosition < minPosition || targetPosition > maxPosition)
                throw IllegalArgumentException("Target position is out of bounds")

            // Determine if lowering
            lowering = targetPosition < shoulder.currentPosition

            // Set power
            shoulder.power = if (lowering) -power else power
        }

        override fun update(packet: TelemetryPacket): Boolean {
            // Get position
            val currentPosition = shoulder.currentPosition
            packet.put("Shoulder position", currentPosition)

            if (lowering) {
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
            // Stop the shoulder
            shoulder.power = 0.0
        }
    }
    fun extend(): Action {
        return Control(0.2, maxPosition)
    }
    fun retract(): Action {
        return Control(maxPower, minPosition)
    }
    fun goTo(position: Int): Action {
        return Control(maxPower, position)
    }

    /**
     * Set the power of the shoulder.
     *
     * @param power the power to set the shoulder to
     */
    fun setPower(power: Double) {
        // Set servo power
        shoulder.power = power
    }

    override fun update(telemetry: Telemetry) {
        telemetry.addData("Shoulder Position", shoulder.currentPosition)
    }
}