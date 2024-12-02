package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap


class Lift(hardwareMap: HardwareMap, rightName: String, leftName: String) : Attachment {
    // Constants
    private val maxPosition: Int = 1900
    private val maxPower: Double = 0.8

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

    private class Control(
        private val liftRight: DcMotor,
        private val liftLeft: DcMotor,
        private val power: Double,
        private val targetPosition: Int
    ) : Action {
        private var initialized = false

        private var lowering = false

        override fun run(p: TelemetryPacket): Boolean {
            if (!initialized) {
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
        return Control(liftRight, liftLeft, maxPower, maxPosition)
    }
    fun drop() : Action {
        return Control(liftRight, liftLeft, maxPower, 100)
    }
    fun lift(position: Int) : Action {
        return Control(liftRight, liftLeft, maxPower, position)
    }
}