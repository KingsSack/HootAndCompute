package com.lasteditguild.volt.attachment

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * CRServoWithPotentiometer is an attachment that controls a continuous rotation servo with a potentiometer.
 *
 * @param hardwareMap for initializing cr servo and potentiometer
 * @param name the name of the cr servo
 * @param potentiometer the potentiometer for the cr servo
 * @param reversed if the cr servo moves the opposite of the potentiometer
 */
open class CRServoWithPotentiometer(
    hardwareMap: HardwareMap,
    private val name: String,
    private val potentiometer: AnalogInput,
    private var reversed: Boolean
) : Attachment() {
    // Initialize cr servo and potentiometer
    protected val crServo: CRServo = hardwareMap.crservo[name]

    init {
        crServos = listOf(crServo)
    }

    /**
     * An action to control the cr servo.
     *
     * @param power the power to set the cr servo to
     * @param position the position to run the cr servo to
     */
    inner class CRServoWithPotentiometer(
        private val power: Double,
        private val position: Double
    ) : ControlAction() {
        private var reversing = false

        override fun init() {
            // Check if the target position is valid
            require(position in 0.0..potentiometer.maxVoltage) { "Position must be between 0 and ${potentiometer.maxVoltage}" }

            // Determine reversing
            reversing = position < potentiometer.voltage

            // Set power
            crServo.power = if (reversing) -power else power
        }

        override fun update(packet: TelemetryPacket): Boolean {
            // Get the current position
            val currentAngle = potentiometer.voltage / potentiometer.maxVoltage
            packet.put("CRServo $name position", currentAngle)
            return ((position > potentiometer.voltage) xor reversing xor reversed)
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
        telemetry.addData("Position", potentiometer.voltage)
    }
}