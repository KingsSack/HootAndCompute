package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.Servo
import dev.kingssack.volt.attachment.Attachment
import dev.kingssack.volt.attachment.ServoAttachment

/**
 * [Classifier] is a [ServoAttachment] that controls a [gate] used for launching artifacts.
 */
class Classifier(gate: Servo) : Attachment("Classifier") {
    fun rotateToNextArtifact(): Action = action {
        loop { true }
    }
}
