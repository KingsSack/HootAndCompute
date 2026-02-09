package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.Servo
import dev.kingssack.volt.annotations.VoltAction
import dev.kingssack.volt.attachment.ServoAttachment
import dev.kingssack.volt.util.ServoPosition

/**
 * [Storage] is a [ServoAttachment] that controls a servo used to release artifacts from storage.
 *
 * @param servo The servo used for storage control
 */
class Storage(servo: Servo) : ServoAttachment("Storage", servo) {
    init {
        servo.position = 0.0
    }

    /**
     * Releases artifacts from the storage.
     *
     * @return An [Action] that moves the servo to the release position
     */
    @VoltAction(name = "Release Storage", description = "Releases Artifacts from the storage")
    fun release(): Action = goTo(ServoPosition(1.0))

    /**
     * Prevents artifacts from being released from the storage.
     *
     * @return An [Action] that moves the servo to the closed position
     */
    @VoltAction(name = "Close Storage", description = "Prevents Artifacts from being released")
    fun close(): Action = goTo(ServoPosition(0.0))
}
