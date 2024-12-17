package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * Elbow is an attachment that bends the Claw.
 *
 * @param hardwareMap for initializing the servo
 * @param name the name of the elbow servo
 *
 * @see Claw
 */
@Config
class Elbow(hardwareMap: HardwareMap, name: String) : Attachment() {
    /**
     * Params is a companion object that holds the configuration for the elbow attachment.
     *
     * @property maxPower the maximum power of the elbow
     * @property timeForFullExtend the time it takes to fully extend the elbow
     */
    companion object Params {
        @JvmField
        var maxPower: Double = 0.72
        @JvmField
        var timeForFullExtend: Double = 1.0
    }

    // Initialize elbow
    private val elbowServo = hardwareMap.crservo[name]

    init {
        crServos = listOf(elbowServo)
    }

    /**
     * Control is an action that bends the elbow.
     *
     * @param power the power to set the servo to
     * @param time the time to set the servo to the power
     */
    inner class Control(
        private val power: Double,
        private val time: Double
    ) : ControlAction() {
        // Runtime
        private val runtime: ElapsedTime = ElapsedTime()

        override fun init() {
            runtime.reset()
            elbowServo.power = power // Set servo power
        }

        override fun update(packet: TelemetryPacket): Boolean {
            return (runtime.seconds() >= time)
        }

        override fun handleStop() {
            // Stop servo
            elbowServo.power = 0.0
        }
    }
    fun extend(): Action {
        return Control(-maxPower, timeForFullExtend)
    }
    fun retract(): Action {
        return Control(maxPower, timeForFullExtend)
    }

    /**
     * Set the power of the claw.
     *
     * @param power the power to set the claw to
     */
    fun setPower(power: Double) {
        // Set servo power
        elbowServo.power = power
    }

    override fun update(telemetry: Telemetry) {
        telemetry.addData("Elbow Power", elbowServo.power)
    }
}