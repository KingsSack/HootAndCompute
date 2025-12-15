package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.PIDFCoefficients
import dev.kingssack.volt.attachment.Attachment
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * [Launcher] is an [Attachment] that controls a dual motor flywheel launcher.
 *
 * @param leftMotor the left [DcMotorEx] of the [Launcher]
 * @param rightMotor the right [DcMotorEx] of the [Launcher]
 */
class Launcher(private val leftMotor: DcMotorEx, private val rightMotor: DcMotorEx) :
    Attachment("Launcher") {
    companion object {
        private const val P = 0.0
        private const val I = 0.0
        private const val D = 0.0
        private const val F = 1.0

        private const val MAX_VELOCITY = 6000.0
        private const val TARGET_VELOCITY = 5000.0
    }

    private val coefficients = PIDFCoefficients(P, I, D, F)

    private var currentVelocity = 0.0

    init {
        leftMotor.direction = DcMotorSimple.Direction.FORWARD
        rightMotor.direction = DcMotorSimple.Direction.REVERSE

        listOf(leftMotor, rightMotor).forEach {
            it.mode = DcMotor.RunMode.RUN_USING_ENCODER
            it.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, coefficients)
        }
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
            addLine("Power ${leftMotor.power} ${rightMotor.power}")
            addLine(
                "Velocity ${leftMotor.velocity}/$TARGET_VELOCITY ${rightMotor.velocity}/$TARGET_VELOCITY"
            )
        }
    }
}
