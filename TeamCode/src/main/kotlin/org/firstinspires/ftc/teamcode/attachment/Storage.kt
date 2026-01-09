package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.Servo
import dev.kingssack.volt.annotations.VoltAction
import dev.kingssack.volt.attachment.ServoAttachment
import dev.kingssack.volt.util.ServoPosition

class Storage(servo: Servo) : ServoAttachment("Storage", servo) {
    @VoltAction(name = "Release Storage", description = "Releases Artifacts from the storage")
    fun release(): Action = goTo(ServoPosition(1.0))

    @VoltAction(name = "Close Storage", description = "Prevents Artifacts from being released")
    fun close(): Action = goTo(ServoPosition(0.0))
}
