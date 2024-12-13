package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * Lift is an attachment that raises and lowers the Claw.
 *
 * @param hardwareMap for initializing motors
 * @param rightName for the right motor
 * @param leftName for the left motor
 *
 * @see Claw
 */
@Config
class Lift(hardwareMap: HardwareMap, rightName: String, leftName: String) : Attachment() {
    companion object Params {
        @JvmField
        var maxPosition: Int = 1900
        @JvmField
        var minPosition: Int = 0
        @JvmField
        var maxPower: Double = 0.8
    }

    // Initialize lifters
    private val liftRight = hardwareMap.dcMotor[rightName]
    private val liftLeft = hardwareMap.dcMotor[leftName]

    init {
        // Set motor directions
        liftRight.direction = DcMotorSimple.Direction.REVERSE
        liftLeft.direction = DcMotorSimple.Direction.FORWARD

        // Set zero power behavior
        liftRight.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        liftLeft.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        // Set motor modes
        liftRight.mode = DcMotor.RunMode.RUN_USING_ENCODER
        liftLeft.mode = DcMotor.RunMode.RUN_USING_ENCODER

        motors = listOf(liftRight, liftLeft)
    }

    /**
     * Control is an action that raises or lowers the lift to a target position.
     *
     * @param power for the power of the lift
     * @param targetPosition for the target position of the lift
     */
    inner class Control(
        private val power: Double,
        private val targetPosition: Int
    ) : ControlAction() {
        private var lowering = false

        override fun init() {
            // Check if the target position is valid
            if (targetPosition < 0 || targetPosition > maxPosition)
                throw IllegalArgumentException("Target position out of bounds")

            // Determine if lowering
            lowering = liftRight.currentPosition > targetPosition

            // Set power
            liftRight.power = if (lowering) -power else power
            liftLeft.power = if (lowering) -power else power
        }

        override fun update(packet: TelemetryPacket): Boolean {
            // Get positions
            val rightPosition: Int = liftRight.currentPosition
            val leftPosition: Int = liftLeft.currentPosition
            packet.put("Lift right position", rightPosition)
            packet.put("Lift left position", leftPosition)

            if (lowering) {
                // Lowering
                if (rightPosition > targetPosition && leftPosition > targetPosition)
                    return true
            } else {
                // Raising
                if (rightPosition < targetPosition && leftPosition < targetPosition)
                    return true
            }

            // At target position
            liftRight.power = 0.0
            liftLeft.power = 0.0
            return false
        }
    }
    fun raise(): Action {
        return Control(maxPower, maxPosition)
    }
    fun drop(): Action {
        return Control(maxPower, minPosition)
    }
    fun goTo(position: Int): Action {
        return Control(maxPower, position)
    }

    override fun update(telemetry: Telemetry) {
        telemetry.addData("Lift Right Position", liftRight.currentPosition)
        telemetry.addData("Lift Left Position", liftLeft.currentPosition)
    }
}