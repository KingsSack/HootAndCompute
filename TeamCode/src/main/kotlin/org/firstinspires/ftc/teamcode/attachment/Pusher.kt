package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.Servo
import dev.kingssack.volt.annotations.VoltAction
import dev.kingssack.volt.attachment.ServoAttachment
import dev.kingssack.volt.util.ServoPosition

/**
 * [Pusher] is a [ServoAttachment] that controls a servo used to push artifacts into the launcher.
 *
 * @param servo The servo used for pushing artifacts
 */
class Pusher(servo: Servo) : ServoAttachment("Pusher", servo) {
    init {
        servo.position = 0.0
    }

    /**
     * Pushes an artifact into the launcher.
     *
     * @return An [Action] that pushes the artifact
     */
    @VoltAction(name = "Push Artifact", description = "Pushes an artifact into the launcher")
    fun push(): Action = goTo(ServoPosition(1.0))

    /**
     * Retracts the pusher.
     *
     * @return An [Action] that retracts the pusher
     */
    @VoltAction(name = "Retract Pusher", description = "Retracts the pusher")
    fun retract(): Action = goTo(ServoPosition(0.0))
}
