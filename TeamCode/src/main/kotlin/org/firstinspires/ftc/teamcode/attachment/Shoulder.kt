package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.Action
import com.lasteditguild.volt.attachment.SimpleAttachmentWithDcMotor
import com.qualcomm.robotcore.hardware.HardwareMap

/**
 * Shoulder is an attachment that extends and retracts the Claw.
 *
 * @param hardwareMap for initializing motor
 * @param name the name of the shoulder motor
 *
 * @see Claw
 */
@Config
class Shoulder(hardwareMap: HardwareMap, name: String) : SimpleAttachmentWithDcMotor(hardwareMap, name) {
    /**
     * Params is a companion object that holds the configuration for the shoulder attachment.
     *
     * @property maxPosition the maximum position of the shoulder
     * @property minPosition the minimum position of the shoulder
     * @property maxPower the maximum power of the shoulder
     */
    companion object Params {
        @JvmField
        var maxPosition: Int = 100
        @JvmField
        var minPosition: Int = 15
        @JvmField
        var maxPower: Double = 0.6
    }

    // Actions
    fun extend(): Action {
        return SimpleAttachmentWithDcMotorControl(0.2, maxPosition, minPosition, maxPosition)
    }
    fun retract(): Action {
        return SimpleAttachmentWithDcMotorControl(maxPower, minPosition, minPosition, maxPosition)
    }
    fun goTo(position: Int): Action {
        return goTo(maxPower, position, minPosition, maxPosition)
    }
}