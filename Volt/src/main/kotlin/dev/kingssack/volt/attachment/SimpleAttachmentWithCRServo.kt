package dev.kingssack.volt.attachment

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * SimpleAttachmentWithCRServo is an attachment that controls a continuous rotation servo.
 *
 * @param hardwareMap for initializing cr servo
 * @param name the name of the cr servo
 */
open class SimpleAttachmentWithCRServo(hardwareMap: HardwareMap, private val name: String) : Attachment() {
    // Initialize cr servo
    protected val crServo: CRServo = hardwareMap.crservo[name]

    init {
        crServos = listOf(crServo)
    }

    /**
     * An action that controls the cr servo by powering it for specified number of [seconds] and at a specified [power].
     */
    inner class SimpleAttachmentWithCRServoControl(
        private val power: Double,
        private val seconds: Double
    ) : ControlAction() {
        // Runtime
        private val runtime: ElapsedTime = ElapsedTime()

        override fun init() {
            runtime.reset()
            crServo.power = power // Set servo power
        }

        override fun update(packet: TelemetryPacket): Boolean {
            return (runtime.seconds() >= seconds)
        }

        override fun handleStop() {
            // Stop servo
            crServo.power = 0.0
        }
    }

    /**
     * Move for a specified amount of [seconds] and a specified [power].
     *
     * @return an action to move the cr servo for a certain amount of time
     */
    fun moveFor(power: Double, seconds: Double): Action {
        return SimpleAttachmentWithCRServoControl(power, seconds)
    }

    /**
     * Set the [power].
     */
    fun setPower(power: Double) {
        require(power in -1.0..1.0) { "Power must be between -1.0 and 1.0" }
        crServo.power = power
    }

    override fun update(telemetry: Telemetry) {
        telemetry.addData("Power", crServo.power)
    }
}