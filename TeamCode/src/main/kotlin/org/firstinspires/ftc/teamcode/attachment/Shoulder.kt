package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.Action
import dev.kingssack.volt.attachment.SimpleAttachmentWithDcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * Shoulder is an attachment that extends and retracts the Claw.
 *
 * @param hardwareMap for initializing motor
 * @param name the name of the shoulder motor
 *
 * @see Claw
 */
@Config
class Shoulder(hardwareMap: HardwareMap, name: String) : SimpleAttachmentWithDcMotor(hardwareMap, name, idlePower, maxPosition, minPosition) {
    /**
     * Params is a companion object that holds the configuration for the shoulder attachment.
     *
     * @property maxPosition the maximum position of the shoulder
     * @property minPosition the minimum position of the shoulder
     * @property extendPower the power of the shoulder for extending
     * @property retractPower the power of the shoulder for retracting
     * @property idlePower the idle power of the shoulder
     */
    companion object Params {
        @JvmField
        var maxPosition: Int = 100
        @JvmField
        var minPosition: Int = 20
        @JvmField
        var extendPower: Double = 0.25
        @JvmField
        var retractPower: Double = 0.45
        @JvmField
        var idlePower: Double = 0.0
    }

    init {
        // Set motor direction
        motor.direction = DcMotorSimple.Direction.REVERSE
    }

    // Actions
    fun extend(): Action {
        return SimpleAttachmentWithDcMotorControl(extendPower, maxPosition)
    }
    fun retract(): Action {
        return SimpleAttachmentWithDcMotorControl(retractPower, minPosition)
    }
    fun goTo(position: Int): Action {
        return goTo(extendPower, position)
    }

    override fun update(telemetry: Telemetry) {
        telemetry.addLine("==== SHOULDER ====")
        super.update(telemetry)
        telemetry.addLine()
    }
}