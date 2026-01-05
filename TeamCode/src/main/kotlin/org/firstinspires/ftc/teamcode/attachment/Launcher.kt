package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.DistanceSensor
import com.qualcomm.robotcore.hardware.PIDFCoefficients
import dev.kingssack.volt.attachment.Attachment
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit

/**
 * [Launcher] is an [Attachment] that controls a dual motor flywheel launcher.
 *
 * @param leftMotor the left [DcMotorEx] of the [Launcher]
 * @param rightMotor the right [DcMotorEx] of the [Launcher]
 * @param distanceSensor the [DistanceSensor] used to measure distance to the target
 */
class Launcher(private val leftMotor: DcMotorEx, private val rightMotor: DcMotorEx, private val distanceSensor: DistanceSensor) :
    Attachment("Launcher") {
    companion object {
        private const val LEFT_P = 40.0
        private const val LEFT_I = 0.0
        private const val LEFT_D = 0.0
        private const val LEFT_F = 13.29
        private const val RIGHT_P = 40.0
        private const val RIGHT_I = 0.0
        private const val RIGHT_D = 0.0
        private const val RIGHT_F = 12.11

        private const val MAX_VELOCITY = 6000.0
        private const val TARGET_VELOCITY = 1500.0
    }

    private val leftCoefficients = PIDFCoefficients(LEFT_P, LEFT_I, LEFT_D, LEFT_F)
    private val rightCoefficients = PIDFCoefficients(RIGHT_P, RIGHT_I, RIGHT_D, RIGHT_F)

    private var currentVelocity = 0.0

    init {
        leftMotor.direction = DcMotorSimple.Direction.FORWARD
        rightMotor.direction = DcMotorSimple.Direction.REVERSE

        listOf(leftMotor, rightMotor).forEach { it.mode = DcMotor.RunMode.RUN_USING_ENCODER }

        leftMotor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, leftCoefficients)
        rightMotor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, rightCoefficients)
    }

    fun enable(): Action = action {
        init { setVelocity(TARGET_VELOCITY) }
        loop { true }
    }

    fun disable(): Action = action {
        init { setVelocity(0.0) }
        loop { true }
    }

    fun increaseVelocity(delta: Double): Action = action {
        init { setVelocity(currentVelocity + delta) }
        loop { true }
    }

    fun decreaseVelocity(delta: Double): Action = action {
        init { setVelocity(currentVelocity - delta) }
        loop { true }
    }

    private fun setVelocity(velocity: Double) {
        require(velocity in 0.0..MAX_VELOCITY) { "Velocity must be between 0.0 and $MAX_VELOCITY, got $velocity" }
        currentVelocity = velocity
    }

    context(telemetry: Telemetry)
    override fun update() {
        super.update()
        listOf(leftMotor, rightMotor).forEach { it.velocity = currentVelocity }
        with(telemetry) {
            addData("Motor Power", "${leftMotor.power} ${rightMotor.power}")
            addData("Motor Velocity", "${leftMotor.velocity} ${rightMotor.velocity}")
            addData("Target Velocity", "$currentVelocity/$TARGET_VELOCITY")
            addData("Measured Distance", distanceSensor.getDistance(DistanceUnit.INCH))
        }
    }
}
