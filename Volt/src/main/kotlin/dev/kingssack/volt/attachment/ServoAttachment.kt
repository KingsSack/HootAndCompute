package dev.kingssack.volt.attachment

import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.Servo
import dev.kingssack.volt.util.ServoPosition
import org.firstinspires.ftc.robotcore.external.Telemetry
import kotlin.math.abs

/**
 * [ServoAttachment] is an [Attachment] that controls a [servo].
 *
 * @param name the name of the servo
 * @param servo the servo to control
 * @param direction the direction of the servo
 */
open class ServoAttachment(
    name: String,
    protected val servo: Servo,
    val direction: Servo.Direction = Servo.Direction.FORWARD,
) : Attachment(name) {
    val position: ServoPosition
        get() = ServoPosition(servo.position)

    init {
        servo.direction = direction
    }

    /**
     * Go to a specified [target] position.
     *
     * @return an action to move the servo to a position
     */
    fun goTo(target: ServoPosition): Action {
        val tolerance = 0.01

        return action {
            init {
                requireReady()
                servo.position = target.value
            }

            loop { abs(position.value - target.value) < tolerance }
        }
    }

    context(telemetry: Telemetry)
    override fun update() {
        super.update()
        telemetry.addData("Position", servo.position)
    }
}
