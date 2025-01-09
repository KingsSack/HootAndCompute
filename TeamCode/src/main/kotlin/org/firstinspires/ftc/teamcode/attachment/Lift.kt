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
        var idlePower: Double = 0.2
        @JvmField
        var timeout: Double = 4.0
        @JvmField
        var coefficients: PIDFCoefficients = PIDFCoefficients(1.0, 1.0, 1.0, 1.0)
    }

    // Initialize lifters
    private val liftRight = hardwareMap.get(DcMotorEx::class.java, rightName)
    private val liftLeft = hardwareMap.get(DcMotorEx::class.java, leftName)

    init {
        // Set motor directions
        liftRight.direction = DcMotorSimple.Direction.REVERSE
        liftLeft.direction = DcMotorSimple.Direction.FORWARD

        // Set zero power behavior
        liftRight.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        liftLeft.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        // Set motor modes
        liftRight.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        liftLeft.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
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

        // Runtime
        private val runtime: ElapsedTime = ElapsedTime()

        override fun init() {
            // Check if the target position is valid
            if (targetPosition < 0 || targetPosition > maxPosition)
                throw IllegalArgumentException("Target position out of bounds")

            // Determine if lowering
            lowering = liftRight.currentPosition > targetPosition

            // Set mode
            liftRight.mode = DcMotor.RunMode.RUN_USING_ENCODER
            liftLeft.mode = DcMotor.RunMode.RUN_USING_ENCODER

            // Set target position
            liftRight.targetPosition = targetPosition
            liftLeft.targetPosition = liftRight.currentPosition

            // Set mode again
            liftRight.mode = DcMotor.RunMode.RUN_TO_POSITION
            liftLeft.mode = DcMotor.RunMode.RUN_TO_POSITION

            // Reset runtime
            runtime.reset()

            // Set power
            liftRight.power = power
            liftLeft.power = power
        }

        override fun update(packet: TelemetryPacket): Boolean {
            // Get positions
            val rightPosition = liftRight.currentPosition
            val leftPosition = liftLeft.currentPosition
            packet.put("Lift right position", rightPosition)
            packet.put("Lift left position", leftPosition)

            // Set target position
            liftLeft.targetPosition = rightPosition

            // Check for timeout
            if (runtime.seconds() > timeout) {
                return true
            }

            if (lowering) {
                // Lowering
                if (rightPosition >= targetPosition && leftPosition >= targetPosition)
                    return false
            } else {
                // Raising
                if (rightPosition <= targetPosition && leftPosition <= targetPosition)
                    return false
            }

            // At target position
            return true
        }

        override fun handleStop() {
            if (liftRight.currentPosition > 10) {
                liftRight.power = idlePower
                liftLeft.power = idlePower
            } else {
                liftRight.power = 0.0
                liftLeft.power = 0.0
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

    fun setPower(power: Double) {
        if (running)
            return

        if (power != 0.0) {
            liftRight.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            liftLeft.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        }

        var finalPower = power
        if (power > maxPower)
            finalPower = maxPower

        if (liftRight.currentPosition > maxPosition || liftLeft.currentPosition > maxPosition)
            finalPower = 0.0

        liftRight.power = finalPower
        liftLeft.power = finalPower
    }

    /**
     * Resets the lift attachment motors.
     *
     * Should only be called when the lift is lowered.
     */
    fun reset() {
        liftRight.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        liftLeft.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
    }

    override fun update(telemetry: Telemetry) {
        telemetry.addLine("==== LIFT ====")
        if (liftRight.isBusy) {
            liftLeft.targetPosition = liftRight.currentPosition
        }
        telemetry.addData("Right Position", liftRight.currentPosition)
        telemetry.addData("Left Position", liftLeft.currentPosition)
        telemetry.addData("Right Busy", liftRight.isBusy)
        telemetry.addData("Left Busy", liftLeft.isBusy)
        telemetry.addLine()
    }
}