package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.Servo
import dev.kingssack.volt.annotations.VoltAction
import dev.kingssack.volt.attachment.ServoAttachment
import dev.kingssack.volt.util.ServoPosition

/**
 * [Pusher] is a [ServoAttachment] that controls a servo used to push artifacts into the launcher.
 *
 * @param servo The servo used for pushing artifacts.
 */
class Pusher(servo: Servo) : ServoAttachment("Pusher", servo) {
    val isExtended: Boolean
        get() = servo.position > 0.0

    @VoltAction(name = "Push Artifact", description = "Pushes an artifact into the launcher")
    fun push(): Action = goTo(ServoPosition(1.0))

    @VoltAction(name = "Retract Pusher", description = "Retracts the pusher")
    fun retract(): Action = goTo(ServoPosition(0.0))
}
