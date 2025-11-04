package dev.kingssack.volt.attachment

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

    /**
     * Move the cr servo for a certain amount of time.
     *
     * @param power the power to set the cr servo to
     * @param angle the angle to move the cr servo to
     *
     * @return an action to move the cr servo to a certain position with a potentiometer
     */
    fun goTo(power: Double, angle: Double): Action {
        require(angle in 0.0..potentiometer.maxVoltage) { "Angle must be between 0 and ${potentiometer.maxVoltage}" }
        require(power in 0.0..1.0) { "Power must be between 0.0 and 1.0" }

        var reversing = false

        return controlAction(
            init = {
                // Determine reversing
                reversing = angle < potentiometer.voltage

                // Set power
                crServo.power = if (reversing) -power else power
            },
            update = { pkt ->
                pkt.put("CRServo $name voltage", potentiometer.voltage)
                pkt.put("CRServo $name target voltage", angle)
                pkt.put("CRServo $name power", crServo.power)
                return@controlAction ((potentiometer.voltage > angle) xor reversing xor reversed)
            },
            onStop = {
                // Stop servo
                crServo.power = 0.0
            }
        )
    }

    /**
     * Set the power of the cr servo.
     *
     * @param power the power to set the cr servo to
     */
    fun setPower(power: Double) {
        if (!running) crServo.power = power
    }

    override fun update(telemetry: Telemetry) {
        telemetry.addData("Power", crServo.power)
        telemetry.addData("Position", potentiometer.voltage)
    }
}