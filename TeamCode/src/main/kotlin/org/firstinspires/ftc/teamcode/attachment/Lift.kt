package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.lasteditguild.volt.attachment.Attachment
import com.qualcomm.robotcore.hardware.*
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * Lift is an attachment that raises and lowers the Claw.
 *
 * Lift can raise, lower, and go to a position.
 * While idling, the lift will maintain its position using its idle power.
 *
 * @param hardwareMap for initializing motors
 * @param rightName for the right motor
 * @param leftName for the left motor
 *
 * @see Claw
 */
@Config
class Lift(hardwareMap: HardwareMap, rightName: String, leftName: String) : Attachment() {
    /**
     * Params is a companion object that holds the configuration for the lift attachment.
     *
     * @property maxPosition the maximum position of the lift
     * @property upperBasketHeight the position of the lift for the upper basket
     * @property lowerBasketHeight the position of the lift for the lower basket
     * @property upperSubmersibleBarHeight the position of the lift for the upper submersible bar
     * @property lowerSubmersibleBarHeight the position of the lift for the lower submersible bar
     * @property minPosition the minimum position of the lift
     * @property maxPower the maximum power of the lift
     * @property idlePower the idle power of the lift
     * @property timeout the timeout for the lift
     * @property coefficients the PIDF coefficients for the lift
     */
    companion object Params {
        @JvmField
        var maxPosition: Int = 2200
        @JvmField
        var upperBasketHeight: Int = 2000
        @JvmField
        var lowerBasketHeight: Int = 500
        @JvmField
        var upperSubmersibleBarHeight: Int = 1200
        @JvmField
        var lowerSubmersibleBarHeight: Int = 500
        @JvmField
        var minPosition: Int = 10
        @JvmField
        var maxPower: Double = 0.6
        @JvmField
        var idlePower: Double = 0.6
        @JvmField
        var timeout: Double = 4.0
        @JvmField
        var coefficients: PIDFCoefficients = PIDFCoefficients(0.9, 1.0, 1.0, 1.0)
    }

    // Initialize lifters
    private val liftRight = hardwareMap.get(DcMotorEx::class.java, rightName)
    private val liftLeft = hardwareMap.get(DcMotorEx::class.java, leftName)

    init {
        // Set motor directions
        liftRight.direction = DcMotorSimple.Direction.REVERSE
        liftLeft.direction = DcMotorSimple.Direction.FORWARD

        // Set coefficients, zero power behavior, and modes
        listOf(liftRight, liftLeft).forEach {
            it.setPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION, coefficients)
            it.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            it.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            it.mode = DcMotor.RunMode.RUN_USING_ENCODER
        }

        motors = listOf(liftRight, liftLeft)
    }

    var currentGoal: Int = 0
        set(value) {
            val temp = value.coerceAtLeast(0)
            field = temp.coerceAtMost(maxPosition)
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
        // Runtime
        private val runtime: ElapsedTime = ElapsedTime()

        override fun init() {
            // Check if the target position is valid
            require(targetPosition in minPosition..maxPosition) { "Target position out of bounds" }

            // Update current goal
            currentGoal = targetPosition

            // Get motors ready
            listOf(liftRight, liftLeft).forEach {
                it.mode = DcMotor.RunMode.RUN_USING_ENCODER
                it.targetPosition = currentGoal
                it.mode = DcMotor.RunMode.RUN_TO_POSITION
                it.power = power
            }

            runtime.reset()
        }

        override fun update(packet: TelemetryPacket): Boolean {
            // Print telemetry
            packet.put("Lift right position", liftRight.currentPosition)
            packet.put("Lift left position", liftLeft.currentPosition)
            packet.put("Lift right target", liftRight.targetPosition)
            packet.put("Lift left target", liftLeft.targetPosition)
            packet.put("Current goal", currentGoal)

            // Has it completed
            return if (runtime.seconds() > timeout) {
                true
            } else {
                !liftRight.isBusy
            }
        }

        override fun handleStop() {
            // Set the motors to idle power
            listOf(liftRight, liftLeft).forEach {
                it.power = idlePower
            }
        }
    }

    // Actions
    fun raise(): Action {
        return Control(maxPower, maxPosition)
    }
    fun drop(): Action {
        return Control(0.4, minPosition)
    }
    fun goTo(position: Int): Action {
        return Control(maxPower, position)
    }

    /**
     * Resets the lift attachment motors.
     *
     * Should only be called when the lift is lowered.
     */
    fun reset() {
        listOf(liftRight, liftLeft).forEach {
            it.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            it.mode = DcMotor.RunMode.RUN_USING_ENCODER
        }
    }

    override fun update(telemetry: Telemetry) {
        if (!running && currentGoal != 0) {
            listOf(liftRight, liftLeft).forEach {
                it.targetPosition = currentGoal
                it.mode = DcMotor.RunMode.RUN_TO_POSITION
                it.power = idlePower
            }
        }

        telemetry.addLine("==== LIFT ====")
        telemetry.addData("Goal", currentGoal)
        telemetry.addData("Right Position", liftRight.currentPosition)
        telemetry.addData("Left Position", liftLeft.currentPosition)
        telemetry.addData("Right Busy", liftRight.isBusy)
        telemetry.addData("Left Busy", liftLeft.isBusy)
        telemetry.addLine()
    }
}