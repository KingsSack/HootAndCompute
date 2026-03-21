package dev.kingssack.volt.attachment

import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.util.ElapsedTime
import dev.kingssack.volt.util.Power
import dev.kingssack.volt.util.Seconds
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * [CRServoAttachment] is an [Attachment] that controls a [crServo].
 *
 * @param name the name of the attachment
 * @param crServo the cr servo to control
 * @param direction the direction of the cr servo
 */
open class CRServoAttachment(
    name: String,
    protected val crServo: CRServo,
    direction: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD,
) : Attachment(name) {
    init {
        crServo.direction = direction
    }

    /**
     * Move for a specified amount of [seconds] and a specified [power].
     *
     * @return an action to move the [crServo] for a certain amount of time
     */
    fun moveFor(power: Power, seconds: Seconds): Action {
        val runtime = ElapsedTime()

        return action {
            init {
                requireReady()
                runtime.reset()
                setPower(power)
            }

            loop { runtime.seconds() >= seconds.value }

            cleanup { stop() }
        }
    }

    /** @see moveFor */
    fun moveFor(power: Double, seconds: Double) = moveFor(Power(power), Seconds(seconds))

    /**
     * Start moving the cr servo at a specified [power].
     *
     * @return an action to start moving the cr servo
     */
    fun start(power: Power): Action = action {
        init { setPower(power) }

        loop { true }
    }

    /** @see start */
    fun start(power: Double) = start(Power(power))

    private fun setPower(power: Power) {
        crServo.power = power.value
    }

    context(telemetry: Telemetry)
    override fun update() {
        super.update()
        telemetry.addData("Power", crServo.power)
    }

    override fun stop() {
        setPower(Power(0.0))
        setState(AttachmentState.Idle)
    }
}
