package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.DistanceSensor
import com.qualcomm.robotcore.hardware.PIDFCoefficients
import dev.kingssack.volt.annotations.VoltAction
import dev.kingssack.volt.attachment.Attachment
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit

/**
 * [Launcher] is an [Attachment] that controls a dual motor flywheel launcher.
 *
 * @param leftMotor the left [DcMotorEx] of the [Launcher]
 * @param rightMotor the right [DcMotorEx] of the [Launcher]
 * @param distanceSensor the [DistanceSensor] used to measure distance to the target
 * @param leftPIDFCoefficients the [PIDFCoefficients] for the left motor
 * @param rightPIDFCoefficients the [PIDFCoefficients] for the right motor
 * @param maxVelocity the maximum velocity of the launcher motors
 * @param targetVelocity the target velocity of the launcher motors
 */
class Launcher(
    private val leftMotor: DcMotorEx,
    private val rightMotor: DcMotorEx,
    private val distanceSensor: DistanceSensor,
    leftPIDFCoefficients: PIDFCoefficients,
    rightPIDFCoefficients: PIDFCoefficients,
    private val maxVelocity: Double,
    private val targetVelocity: Double,
) : Attachment("Launcher") {
    private var currentVelocity = 0.0

    init {
        leftMotor.direction = DcMotorSimple.Direction.FORWARD
        rightMotor.direction = DcMotorSimple.Direction.REVERSE

        listOf(leftMotor, rightMotor).forEach { it.mode = DcMotor.RunMode.RUN_USING_ENCODER }

        leftMotor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, leftPIDFCoefficients)
        rightMotor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, rightPIDFCoefficients)
    }

    @VoltAction(name = "Enable Launcher", description = "Enables the launcher at target velocity")
    fun enable(): Action = action {
        init { setVelocity(targetVelocity) }
        loop { true }
    }

    @VoltAction(name = "Disable Launcher", description = "Disables the launcher")
    fun disable(): Action = action {
        init { setVelocity(0.0) }
        loop { true }
    }

    @VoltAction(
        name = "Increase Launcher Velocity",
        description = "Increases the launcher velocity by the specified delta",
    )
    fun increaseVelocity(delta: Double): Action = action {
        init { setVelocity(currentVelocity + delta) }
        loop { true }
    }

    @VoltAction(
        name = "Decrease Launcher Velocity",
        description = "Decreases the launcher velocity by the specified delta",
    )
    fun decreaseVelocity(delta: Double): Action = action {
        init { setVelocity(currentVelocity - delta) }
        loop { true }
    }

    private fun setVelocity(velocity: Double) {
        require(velocity in 0.0..maxVelocity) {
            "Velocity must be between 0.0 and $maxVelocity, got $velocity"
        }
        currentVelocity = velocity
    }

    context(telemetry: Telemetry)
    override fun update() {
        super.update()
        listOf(leftMotor, rightMotor).forEach { it.velocity = currentVelocity }
        with(telemetry) {
            addData("Motor Power", "${leftMotor.power} ${rightMotor.power}")
            addData("Motor Velocity", "${leftMotor.velocity} ${rightMotor.velocity}")
            addData("Target Velocity", "$currentVelocity/$targetVelocity")
            addData("Measured Distance", distanceSensor.getDistance(DistanceUnit.INCH))
        }
    }
}
