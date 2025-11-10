package dev.kingssack.volt.attachment

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
open class SimpleAttachmentWithDcMotor(
    hardwareMap: HardwareMap,
    private val name: String,
    private val idlePower: Double,
    private val maxPosition: Int,
    private val minPosition: Int = 0,
) : Attachment() {
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
    }

    var currentGoal: Int = 0
        set(value) {
            val temp = value.coerceAtLeast(minPosition)
            field = temp.coerceAtMost(maxPosition)
        }

    /** Resets the motor's encoder. */
    fun reset() {
        motor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        motor.mode = DcMotor.RunMode.RUN_USING_ENCODER
    }

    /**
     * Move to a [position] at a specified [power].
     *
     * @return an action to move the motor to a position
     */
    fun goTo(power: Double, position: Int): Action {
        require(position in minPosition..maxPosition)

        return controlAction(
            init = {
                // Update current goal
                currentGoal = position

                // Set target position
                motor.mode = DcMotor.RunMode.RUN_USING_ENCODER
                motor.targetPosition = currentGoal
                motor.mode = DcMotor.RunMode.RUN_TO_POSITION

                // Set power
                motor.power = power
            },
            update = { pkt ->
                pkt.put("DcMotor $name position", motor.currentPosition)
                pkt.put("DcMotor $name target", motor.targetPosition)
                return@controlAction !motor.isBusy
            },
            onStop = {
                // Stop the motor
                motor.power = idlePower
            },
        )
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