package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * Wrist is an attachment that twists the Claw.
 *
 * @param hardwareMap for initializing the servo
 * @param name the name of the wrist servo
 *
 * @see Claw
 */
@Config
class Wrist(hardwareMap: HardwareMap, name: String) : Attachment() {
    /**
     * WristParams is a configuration object for the wrist.
     *
     * @property minPosition the minimum position of the wrist
     * @property maxPosition the maximum position of the wrist
     */
    companion object Params {
        @JvmField
        var minPosition: Double = 0.0
        @JvmField
        var maxPosition: Double = 1.0
    }

    // Initialize wrist
    private val wristServo = hardwareMap.servo[name]

    init {
        servos = listOf(wristServo)
    }

    /**
     * Control is an action that twists the wrist.
     *
     * @param targetPosition the position to set the servo to
     */
    inner class Control(
        private val targetPosition: Double
    ) : ControlAction() {
        override fun init() {
            wristServo.position = targetPosition
        }

        override fun update(packet: TelemetryPacket): Boolean {
            return false
        }
    }
    fun twistTo(position: Double): Action {
        return Control(position)
    }

    /**
     * Get the position of the extender.
     *
     * @return the position of the extender
     */
    fun getPosition(): Double {
        return wristServo.position
    }

    override fun update(telemetry: Telemetry) {
        telemetry.addData("Wrist Position", wristServo.position)
    }
}