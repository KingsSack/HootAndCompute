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
        var maxPosition: Int = 0
        @JvmField
        var minPosition: Int = 0
        @JvmField
        var maxPower: Double = 0.8
    }

    // Initialize shoulder motor
    private val shoulder = hardwareMap.dcMotor[name]

    init {
        // Set motor direction
        shoulder.direction = DcMotorSimple.Direction.FORWARD

        // Set zero power behavior
        shoulder.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        // Set motor mode
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
            if (targetPosition < 0 || targetPosition > maxPosition)
                throw IllegalArgumentException("Target position is out of bounds")

            // Check if the goTo is raising or lowering
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
                    return true
            } else {
                // Raising
                if (currentPosition < targetPosition)
                    return true
            }

            // At target position
            shoulder.power = 0.0
            return false
        }
    }
    fun extend(): Action {
        return Control(maxPower, maxPosition)
    }
    fun retract(): Action {
        return Control(maxPower, minPosition)
    }
    fun goTo(position: Int): Action {
        return Control(maxPower, position)
    }


    override fun update(telemetry: Telemetry) {
        telemetry.addData("Shoulder Position", shoulder.currentPosition)
    }
}