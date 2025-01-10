package com.lasteditguild.volt.attachment

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry

open class CRServoWithPotentiometer(hardwareMap: HardwareMap, private val crServoName: String, private val potentiometer: AnalogInput, protected var isReversed: Boolean) : Attachment() {
    // Initialize cr servo and potentiometer
    protected val crServo: CRServo = hardwareMap.crservo[crServoName]

    init {
        crServos = listOf(crServo)
    }

    /**
     * An action to control the cr servo.
     *
     * @param power the power to set the cr servo to
     * @param angle the angle to run the cr servo to
     */
    inner class CRServoWithPotentiometer(
        private val power: Double,
        private val angle: Double
    ) : ControlAction() {
        // Runtime

        private var isForwardDirection: Boolean = false

        override fun init() {
            isForwardDirection = angle>(potentiometer.voltage/potentiometer.maxVoltage)
            crServo.power = power // Set servo power
        }

        override fun update(packet: TelemetryPacket): Boolean {
            return (angle > (potentiometer.voltage/potentiometer.maxVoltage) ==isForwardDirection xor isReversed)
        }

        override fun handleStop() {
            // Stop servo
            crServo.power = 0.0
        }
    }

    /**
     * Move the cr servo for a certain amount of time.
     *
     * @param angle the power to set the cr servo to
     * @param seconds the amount of time to run the cr servo
     *
     * @return an action to move the cr servo for a certain amount of time
     */
    fun moveFor(angle: Double, seconds: Double): Action {
        return CRServoWithPotentiometer(angle, seconds)
    }

    /**
     * Set the power of the cr servo.
     *
     * @param power the power to set the cr servo to
     */
    fun setPower(power: Double) {
        crServo.power = power
    }

    override fun update(telemetry: Telemetry) {
        telemetry.addData("Power", crServo.power)
    }
}