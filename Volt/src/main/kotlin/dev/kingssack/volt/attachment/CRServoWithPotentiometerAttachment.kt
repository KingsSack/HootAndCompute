package dev.kingssack.volt.attachment

import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.DcMotorSimple
import dev.kingssack.volt.util.Power
import dev.kingssack.volt.util.Voltage
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * [CRServoWithPotentiometerAttachment] is an [Attachment] that controls a [crServo] with a
 * [potentiometer].
 *
 * @param name the name of the cr servo
 * @param crServo the cr servo to control
 * @param potentiometer the potentiometer reading the position of the cr servo
 * @param direction the direction of the cr servo
 */
open class CRServoWithPotentiometerAttachment(
    name: String,
    crServo: CRServo,
    protected val potentiometer: AnalogInput,
    direction: DcMotorSimple.Direction,
) : CRServoAttachment(name, crServo, direction) {
    /**
     * Move to a specified [target] at a specified [power].
     *
     * @return an action to move the [crServo] to a certain position using a [potentiometer]
     */
    fun goTo(power: Power, target: Voltage): Action {
        require(target.value in 0.0..potentiometer.maxVoltage) {
            "Voltage must be between 0 and ${potentiometer.maxVoltage}"
        }

        var reversing = false

        return action {
            init {
                requireReady()
                reversing = target.value < potentiometer.voltage
                crServo.power = if (reversing) -power.value else power.value
            }

            loop {
                put("CRServo $name voltage", potentiometer.voltage)
                put("CRServo $name target voltage", target.value)
                put("CRServo $name power", crServo.power)
                (potentiometer.voltage > target.value) xor reversing
            }

            cleanup { stop() }
        }
    }

    context(telemetry: Telemetry)
    override fun update() {
        super.update()
        telemetry.addData("Position", potentiometer.voltage)
    }
}
