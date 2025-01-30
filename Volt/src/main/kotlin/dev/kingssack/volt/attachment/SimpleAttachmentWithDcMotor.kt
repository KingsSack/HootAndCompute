package dev.kingssack.volt.attachment

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * SimpleAttachmentWithDcMotor is an attachment that controls a motor.
 *
 * @param hardwareMap for registering the motor
 * @param name the name of the motor
 * @param idlePower the idle power of the motor
 * @param maxPosition the maximum position of the motor
 * @param minPosition the minimum position of the motor
 */
open class SimpleAttachmentWithDcMotor(hardwareMap: HardwareMap, private val name: String, private val idlePower: Double, private val maxPosition: Int, private val minPosition: Int = 0) : Attachment() {
    // Initialize motor
    protected val motor: DcMotor = hardwareMap.dcMotor[name]

    init {
        // Set motor direction
        motor.direction = DcMotorSimple.Direction.FORWARD

        // Set zero power behavior
        motor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        // Set motor mode
        motor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        motor.mode = DcMotor.RunMode.RUN_USING_ENCODER

        motors = listOf(motor)
    }

    var currentGoal: Int = 0
        set(value) {
            val temp = value.coerceAtLeast(minPosition)
            field = temp.coerceAtMost(maxPosition)
        }

    /**
     * An action that extends or retracts the motor to a target position.
     *
     * @param power for the power of the motor
     * @param targetPosition for the target position of the motor
     */
    inner class SimpleAttachmentWithDcMotorControl(
        private val power: Double,
        private val targetPosition: Int,
    ) : ControlAction() {
        override fun init() {
            // Check if the target position is valid
            require(targetPosition in minPosition..maxPosition) { "Target position out of bounds" }

            // Update current goal
            currentGoal = targetPosition

            // Set target position
            motor.mode = DcMotor.RunMode.RUN_USING_ENCODER
            motor.targetPosition = currentGoal
            motor.mode = DcMotor.RunMode.RUN_TO_POSITION

            // Set power
            motor.power = power
        }

        override fun update(packet: TelemetryPacket): Boolean {
            // Get position
            packet.put("DcMotor $name position", motor.currentPosition)
            packet.put("DcMotor $name target", motor.targetPosition)

            // Has it completed
            return !motor.isBusy
        }

        override fun handleStop() {
            // Stop the motor
            motor.power = idlePower
        }
    }

    /**
     * Resets the motor's encoder.
     */
    fun reset() {
        motor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        motor.mode = DcMotor.RunMode.RUN_USING_ENCODER
    }

    /**
     * Move the motor.
     *
     * @param power the power to set the motor to
     * @param position the target position of the motor
     *
     * @return an action to move the motor to a position
     */
    fun goTo(power: Double, position: Int): Action {
        return SimpleAttachmentWithDcMotorControl(power, position)
    }

    override fun update(telemetry: Telemetry) {
        if (!running) {
            motor.targetPosition = currentGoal
            motor.mode = DcMotor.RunMode.RUN_TO_POSITION
            motor.power = idlePower
        }

        telemetry.addData("Goal", currentGoal)
        telemetry.addData("Position", motor.currentPosition)
        telemetry.addData("Busy", motor.isBusy)
    }
}