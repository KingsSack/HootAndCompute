package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.teamcode.Configuration

/**
 * Lift is an attachment that raises and lowers the claw.
 *
 * @param hardwareMap for initializing motors
 * @param rightName for the right motor
 * @param leftName for the left motor
 */
class Lift(hardwareMap: HardwareMap, rightName: String, leftName: String) : Attachment {
    // Constants
    private val maxPosition: Int = Configuration.liftParams.maxPosition
    private val maxPower: Double = Configuration.liftParams.maxPower

    // Initialize lifters
    private val liftRight = hardwareMap.get(DcMotor::class.java, rightName)
    private val liftLeft = hardwareMap.get(DcMotor::class.java, leftName)

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
    }

    /**
     * Control is an action that raises or lowers the lift to a target position.
     *
     * @param liftRight for the right motor
     * @param liftLeft for the left motor
     * @param power for the power of the lift
     * @param targetPosition for the target position of the lift
     * @param maxPosition for the maximum position of the lift
     */
    private class Control(
        private val liftRight: DcMotor,
        private val liftLeft: DcMotor,
        private val power: Double,
        private val targetPosition: Int,
        private val maxPosition: Int
    ) : Action {
        private var initialized = false

        private var lowering = false

        override fun run(p: TelemetryPacket): Boolean {
            if (!initialized) {
                // Check if the target position is valid
                if (targetPosition < 0 || targetPosition > maxPosition)
                    throw IllegalArgumentException("Target position out of bounds")

                // Determine if lowering
                lowering = liftRight.currentPosition > targetPosition

                // Set power
                if (lowering) {
                    liftRight.power = -power
                    liftLeft.power = -power
                }
                else {
                    liftRight.power = power
                    liftLeft.power = power
                }

                initialized = true
            }

            // Get positions
            val rightPosition: Int = liftRight.currentPosition
            val leftPosition: Int = liftLeft.currentPosition
            p.put("Lift right position", rightPosition)
            p.put("Lift left position", leftPosition)

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
    fun raise() : Action {
        return Control(liftRight, liftLeft, maxPower, maxPosition, maxPosition)
    }
    fun drop() : Action {
        return Control(liftRight, liftLeft, maxPower, 10, maxPosition)
    }
    fun lift(position: Int) : Action {
        return Control(liftRight, liftLeft, maxPower, position, maxPosition)
    }
}