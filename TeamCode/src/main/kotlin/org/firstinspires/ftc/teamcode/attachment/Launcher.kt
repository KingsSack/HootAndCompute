package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.DistanceSensor
import com.qualcomm.robotcore.hardware.PIDFCoefficients
import dev.kingssack.volt.annotations.VoltAction
import dev.kingssack.volt.attachment.Attachment
import kotlin.math.abs
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
 * @property currentVelocity the current target velocity of the launcher motors
 * @property averageSpinUpTime the average time taken to spin up to target velocity
 * @property averageVelocityDelta the average difference in velocity between the left and right
 *   motors
 * @property isAtSpeed whether the launcher is at the target velocity
 * @property isStopped whether the launcher is stopped
 * @property velocityDelta the difference in velocity between the left and right motors
 * @property isOverheating whether either motor is overheating
 */
class Launcher(
    private val leftMotor: DcMotorEx,
    private val rightMotor: DcMotorEx,
    private val distanceSensor: DistanceSensor,
    leftPIDFCoefficients: PIDFCoefficients,
    rightPIDFCoefficients: PIDFCoefficients,
    private val maxVelocity: Double,
    private val targetVelocity: Double,
    private val velocityTolerance: Double = 40.0,
) : Attachment("Launcher") {
    private var spinUpStartTime: Long = 0
    private var lastSpinUpTime: Long = 0
    private var isSpinningUp: Boolean = false
    private val spinUpTimes = mutableListOf<Long>()
    private var velocityDeltaSampleCount = 0
    private var velocityDeltaSum = 0.0
    private var maxVelocityDelta = Double.NEGATIVE_INFINITY
    private var minVelocityDelta = Double.POSITIVE_INFINITY

    var currentVelocity = 0.0
        private set

    val averageSpinUpTime: Double
        get() = if (spinUpTimes.isEmpty()) 0.0 else spinUpTimes.average()

    val averageVelocityDelta: Double
        get() =
            if (velocityDeltaSampleCount == 0) 0.0 else velocityDeltaSum / velocityDeltaSampleCount

    val isAtSpeed: Boolean
        get() {
            val leftError = abs(leftMotor.velocity - currentVelocity)
            val rightError = abs(rightMotor.velocity - currentVelocity)
            val atSpeed = leftError < velocityTolerance && rightError < velocityTolerance

            if (atSpeed && isSpinningUp) {
                lastSpinUpTime = System.currentTimeMillis() - spinUpStartTime
                spinUpTimes.add(lastSpinUpTime)
                isSpinningUp = false
            }

            return atSpeed
        }

    val isStopped: Boolean
        get() = leftMotor.velocity == 0.0 && rightMotor.velocity == 0.0

    val velocityDelta: Double
        get() = leftMotor.velocity - rightMotor.velocity

    val isOverheating: Boolean
        get() = leftMotor.isOverCurrent || rightMotor.isOverCurrent

    init {
        leftMotor.direction = DcMotorSimple.Direction.FORWARD
        rightMotor.direction = DcMotorSimple.Direction.REVERSE

        listOf(leftMotor, rightMotor).forEach {
            it.mode = DcMotor.RunMode.RUN_USING_ENCODER
            it.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
        }

        leftMotor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, leftPIDFCoefficients)
        rightMotor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, rightPIDFCoefficients)
    }

    /**
     * Enables the launcher to spin up to the [target] velocity.
     *
     * @return an [Action] that enables the launcher
     */
    @VoltAction(name = "Enable Launcher", description = "Enables the launcher at target velocity")
    fun enable(target: Double = targetVelocity): Action = action {
        init { setVelocity(target) }
        loop {
            put("Left flywheel velocity", leftMotor.velocity)
            put("Right flywheel velocity", rightMotor.velocity)

            isAtSpeed
        }
    }

    /**
     * Disables the launcher by setting the velocity to 0.
     *
     * @return an [Action] that disables the launcher
     */
    @VoltAction(name = "Disable Launcher", description = "Disables the launcher")
    fun disable(): Action = action {
        init { setVelocity(0.0) }
        loop {
            put("Left flywheel velocity", leftMotor.velocity)
            put("Right flywheel velocity", rightMotor.velocity)

            isStopped
        }
    }

    private fun setVelocity(velocity: Double) {
        require(velocity in 0.0..maxVelocity) {
            "Velocity must be between 0.0 and $maxVelocity, got $velocity"
        }

        if (currentVelocity == velocity) return

        currentVelocity = velocity

        // Start tracking spin-up time when changing to non-zero velocity
        if (velocity > 0.0 && !isSpinningUp) {
            spinUpStartTime = System.currentTimeMillis()
            isSpinningUp = true
        }

        listOf(leftMotor, rightMotor).forEach { it.velocity = currentVelocity }
    }

    context(telemetry: Telemetry)
    override fun update(): Unit =
        with(telemetry) {
            super.update()

            val currentDelta = velocityDelta
            velocityDeltaSum += currentDelta
            velocityDeltaSampleCount++

            if (currentDelta > maxVelocityDelta) {
                maxVelocityDelta = currentDelta
            }
            if (currentDelta < minVelocityDelta) {
                minVelocityDelta = currentDelta
            }

            addLine(">>STATUS<<")
            addData("Status", if (isAtSpeed) "✓ READY" else "⏳ SPINNING UP")
            addData("Overheating", if (isOverheating) "⚠ OVERHEATING" else "✓ NORMAL")
            addData(
                "Power",
                if (leftMotor.power == 1.0 || rightMotor.power == 1.0) "⚠ MAX" else "✓ OK",
            )

            addLine()
            addLine(">>VELOCITIES<<")
            addData("Target", "%.1f".format(currentVelocity))
            addData("Left Motor", "%.1f".format(leftMotor.velocity))
            addData("Right Motor", "%.1f".format(rightMotor.velocity))
            addData("Delta", "%.2f (avg: %.2f)".format(currentDelta, averageVelocityDelta))
            addData(
                "Delta Range",
                "[%.2f, %.2f]"
                    .format(
                        if (minVelocityDelta == Double.POSITIVE_INFINITY) 0.0 else minVelocityDelta,
                        if (maxVelocityDelta == Double.NEGATIVE_INFINITY) 0.0 else maxVelocityDelta,
                    ),
            )

            addLine()
            if (isSpinningUp) {
                addLine(">>TIMINGS<<")
                val currentSpinUpTime = System.currentTimeMillis() - spinUpStartTime
                addData("Spin-Up", "%dms (in progress)".format(currentSpinUpTime))
                addData("Avg Spin-Up", "%.0fms".format(averageSpinUpTime))
            } else if (lastSpinUpTime > 0) {
                addLine(">>TIMINGS<<")
                addData("Last Spin-Up", "%dms".format(lastSpinUpTime))
                addData("Avg Spin-Up", "%.0fms".format(averageSpinUpTime))
            }

            addLine()
            addLine(">>SENSOR<<")
            addData("Measured Distance", distanceSensor.getDistance(DistanceUnit.INCH))
        }
}
