package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.hardware.PIDFCoefficients
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
     * @property minPosition the minimum position of the lift
     * @property maxPower the maximum power of the lift
     * @property idlePower the idle power of the lift
     */
    companion object Params {
        @JvmField
        var maxPosition: Int = 2150
        @JvmField
        var upperBasketHeight: Int = 2000
        @JvmField
        var lowerBasketHeight: Int = 1200
        @JvmField
        var minPosition: Int = 10
        @JvmField
        var maxPower: Double = 0.5
        @JvmField
        var idlePower: Double = 0.1
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

    private var running = false
    private var lastPosition = liftRight.currentPosition

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

            // Reset runtime
            runtime.reset()

            // Set power
            liftRight.power = if (lowering) -power else power
            liftLeft.power = if (lowering) -power else power

            running = true
        }

        override fun update(packet: TelemetryPacket): Boolean {
            // Get positions
            val rightPosition = liftRight.currentPosition
            val leftPosition = liftLeft.currentPosition
            packet.put("Lift right position", rightPosition)
            packet.put("Lift left position", leftPosition)

            if (runtime.seconds() > timeout) {
                return true
            }

            if (lowering) {
                // Lowering
                if (rightPosition > targetPosition && leftPosition > targetPosition)
                    return false
            } else {
                // Raising
                if (rightPosition < targetPosition && leftPosition < targetPosition)
                    return false
            }

            // At target position
            return true
        }

        override fun handleStop() {
            liftRight.power = 0.0
            liftLeft.power = 0.0
            if (lowering) {
                reset()
            }
            lastPosition = targetPosition
            running = false
            liftRight.targetPosition = lastPosition
            liftLeft.targetPosition = lastPosition
            liftRight.mode = DcMotor.RunMode.RUN_TO_POSITION
            liftLeft.mode = DcMotor.RunMode.RUN_TO_POSITION
            liftRight.power = idlePower
            liftLeft.power = idlePower
        }
    }
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
        liftRight.power = power
        liftLeft.power = power
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
        if (!running && liftRight.currentPosition > 10 && !liftRight.isBusy && !liftLeft.isBusy) {
            liftLeft.targetPosition = liftRight.currentPosition
            liftRight.power = idlePower
            liftLeft.power = idlePower
        }
        telemetry.addData("Lift Right Position", liftRight.currentPosition)
        telemetry.addData("Lift Left Position", liftLeft.currentPosition)
        telemetry.addData("Lift Right Busy", liftRight.isBusy)
        telemetry.addData("Lift Left Busy", liftLeft.isBusy)
    }
}