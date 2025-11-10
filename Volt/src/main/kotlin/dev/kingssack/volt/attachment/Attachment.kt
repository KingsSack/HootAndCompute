package dev.kingssack.volt.attachment

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.Servo
import dev.kingssack.volt.robot.Robot
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf
import org.firstinspires.ftc.robotcore.external.Telemetry

/** Represents an attachment to the robot. */
abstract class Attachment {
    private var motors: List<DcMotor> =
        this::class
            .memberProperties
            .filter { it.returnType.isSubtypeOf(typeOf<DcMotor>()) }
            .mapNotNull { property ->
                try {
                    property.getter.call(this) as? DcMotor
                } catch (_: Throwable) {
                    null
                }
            }
    private var servos: List<Servo> =
        this::class
            .memberProperties
            .filter { it.returnType.isSubtypeOf(typeOf<Servo>()) }
            .mapNotNull { property ->
                try {
                    property.getter.call(this) as? Servo
                } catch (_: Throwable) {
                    null
                }
            }
    private var crServos =
        this::class
            .memberProperties
            .filter { it.returnType.isSubtypeOf(typeOf<CRServo>()) }
            .mapNotNull { property ->
                try {
                    property.getter.call(this) as? CRServo
                } catch (_: Throwable) {
                    null
                }
            }

    protected var running = false
    protected var robot: Robot? = null

    open fun onRegister(robot: Robot) {
        this.robot = robot
    }

    /** Represents an action that can be run on the attachment. */
    protected fun controlAction(
        init: (() -> Unit)? = null,
        update: (TelemetryPacket) -> Boolean,
        onStop: (() -> Unit)? = null,
    ): Action =
        object : Action {
            private var initialized = false

            override fun run(p: TelemetryPacket): Boolean {
                if (!initialized) {
                    try {
                        init?.invoke()
                    } finally {
                        initialized = true
                        running = true
                    }
                }

                val finished =
                    try {
                        update(p)
                    } catch (t: Throwable) {
                        running = false
                        throw t
                    }

                if (finished) {
                    try {
                        onStop?.invoke()
                    } finally {
                        running = false
                    }
                }

                return finished
            }
        }

    /**
     * Updates the attachment.
     *
     * @param telemetry for logging
     */
    abstract fun update(telemetry: Telemetry)

    /** Stops the attachment. */
    open fun stop() {
        motors.forEach { it.power = 0.0 }
        crServos.forEach { it.power = 0.0 }
    }
}
