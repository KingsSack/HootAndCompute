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
        var idlePower: Double = 0.4
        @JvmField
        var timeout: Double = 5.0
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

    private var currentGoal: Int = 0

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
            if (targetPosition < 0 || targetPosition > maxPosition)
                throw IllegalArgumentException("Target position out of bounds")

            // Determine if lowering
            currentGoal = targetPosition

            // Set mode
            liftRight.mode = DcMotor.RunMode.RUN_USING_ENCODER
            liftLeft.mode = DcMotor.RunMode.RUN_USING_ENCODER

            // Set target position
            liftRight.targetPosition = currentGoal
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
            // Print telemetry
            packet.put("Lift right position", liftRight.currentPosition)
            packet.put("Lift left position", liftLeft.currentPosition)
            packet.put("Lift right target", liftRight.targetPosition)
            packet.put("Lift left target", liftLeft.targetPosition)
            packet.put("Current goal", currentGoal)

            // Set target position
            liftLeft.targetPosition = liftRight.currentPosition

            // Check for timeout
            if (runtime.seconds() > timeout)
                return true

            // Has it completed
            if (liftRight.isBusy)
                return false

            // At target position
            return true
        }

        override fun handleStop() {
            liftRight.power = idlePower
            liftLeft.power = idlePower
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

        currentGoal = liftRight.currentPosition
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
        if (!running && currentGoal != 0) {
            liftRight.mode = DcMotor.RunMode.RUN_TO_POSITION
            liftLeft.mode = DcMotor.RunMode.RUN_TO_POSITION
            liftRight.power = idlePower
            liftLeft.power = idlePower
            if (liftRight.isBusy) {
                liftRight.targetPosition = currentGoal
                liftLeft.targetPosition = liftRight.currentPosition
            }
        }


        telemetry.addLine("==== LIFT ====")
        telemetry.addData("Right Position", liftRight.currentPosition)
        telemetry.addData("Left Position", liftLeft.currentPosition)
        telemetry.addData("Right Busy", liftRight.isBusy)
        telemetry.addData("Left Busy", liftLeft.isBusy)
        telemetry.addLine()
    }
}