package com.lasteditguild.volt.attachment

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry

open class CRServoWithPotentiometer(hardwareMap: HardwareMap, private val name: String, private val potentiometer: AnalogInput, private var reversed: Boolean) : Attachment() {
    // Initialize cr servo and potentiometer
    protected val crServo: CRServo = hardwareMap.crservo[name]

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
        private var reversing = false

        override fun init() {
            // Check if the target position is valid
            if (angle < 0.0 || angle > 1.0)
                throw IllegalArgumentException("Target position is out of bounds")

            // Determine reversing
            reversing = angle < potentiometer.voltage / potentiometer.maxVoltage

            // Set power
            crServo.power = if (reversing) -power else power
        }

        override fun update(packet: TelemetryPacket): Boolean {
            // Get the current angle
            val currentAngle = potentiometer.voltage / potentiometer.maxVoltage
            packet.put("CRServo $name angle", currentAngle)
            return ((angle > (potentiometer.voltage / potentiometer.maxVoltage)) xor reversing xor reversed)
        }

        override fun handleStop() {
            // Stop servo
            crServo.power = 0.0
        }
    }

    /**
     * Move the cr servo for a certain amount of time.
     *
     * @param power the power to set the cr servo to
     * @param angle the angle to move the cr servo to
     *
     * @return an action to move the cr servo to a certain position with a potentiometer
     */
    fun goTo(power: Double, angle: Double): Action {
        return CRServoWithPotentiometer(power, angle)
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