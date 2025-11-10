package dev.kingssack.volt.attachment

import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * [DcMotorAttachment] is an Attachment that controls a [motor].
 *
 * @param name the name of the attachment
 * @param motor the motor to control
 * @param idlePower the idle power of the motor
 * @param maxPosition the maximum position of the motor
 * @param minPosition the minimum position of the motor
 */
open class DcMotorAttachment(
    name: String,
    protected val motor: DcMotor,
    private val idlePower: Double,
    private val maxPosition: Int,
    private val minPosition: Int = 0,
    private val direction: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD,
) : Attachment(name) {
    init {
        // Set motor direction
        motor.direction = direction

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
    fun reset() = action {
        init {
            motor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            motor.mode = DcMotor.RunMode.RUN_USING_ENCODER
        }
        loop { true }
    }

    /**
     * Move to a [position] at a specified [power].
     *
     * @return an action to move the motor to a position
     */
    fun goTo(power: Double, position: Int): Action {
        require(position in minPosition..maxPosition)

        return action {
            init {
                // Update current goal
                currentGoal = position

                // Set target position
                motor.mode = DcMotor.RunMode.RUN_USING_ENCODER
                motor.targetPosition = currentGoal
                motor.mode = DcMotor.RunMode.RUN_TO_POSITION

                // Set power
                motor.power = power
            }

            loop {
                put("DcMotor $name position", motor.currentPosition)
                put("DcMotor $name target", motor.targetPosition)

                !motor.isBusy
            }

            cleanup {
                // Stop the motor
                motor.power = idlePower
            }
        }
    }

    context(telemetry: Telemetry)
    override fun update() {
        if (state.value == AttachmentState.Idle) {
            motor.targetPosition = currentGoal
            motor.mode = DcMotor.RunMode.RUN_TO_POSITION
            motor.power = idlePower
        }

        super.update()
        telemetry.addData("Goal", currentGoal)
        telemetry.addData("Position", motor.currentPosition)
        telemetry.addData("Busy", motor.isBusy)
    }

    override fun stop() {
        motor.power = 0.0
        setState(AttachmentState.Idle)
    }
}
