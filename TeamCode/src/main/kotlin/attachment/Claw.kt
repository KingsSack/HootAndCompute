package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * Claw is an attachment that can open and close.
 *
 * @param hardwareMap the hardware map
 * @param name the name of the claw servo
 */
@Config
class Claw(hardwareMap: HardwareMap, name: String) : Attachment() {
    companion object Params {
        @JvmField
        var maxPower: Double = 0.72
        @JvmField
        var openCloseTime: Double = 0.6
    }

    // Initialize claw
    private val clawServo: CRServo = hardwareMap.crservo[name]

    init {
        crServos = listOf(clawServo)
    }

    /**
     * Control is an action that opens or closes the claw.
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
            clawServo.power = power // Set servo power
        }

        override fun update(packet: TelemetryPacket): Boolean {
            if (runtime.seconds() < time)
                return true

            // Stop servo
            clawServo.power = 0.0
            return false
        }
    }
    fun open(): Action {
        return Control(maxPower, openCloseTime)
    }
    fun close(): Action {
        return Control(-maxPower, openCloseTime)
    }

    /**
     * Set the power of the claw.
     *
     * @param power the power to set the claw to
     */
    fun setPower(power: Double) {
        // Set servo power
        clawServo.power = power
    }

    override fun update(telemetry: Telemetry) {
        telemetry.addData("Claw power", clawServo.power)
    }
}