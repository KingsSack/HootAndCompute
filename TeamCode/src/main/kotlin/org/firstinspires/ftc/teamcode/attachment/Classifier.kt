package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.attachment.Attachment
import org.firstinspires.ftc.robotcore.external.Telemetry

class Classifier(hardwareMap: HardwareMap) : Attachment() {
    private val servo = hardwareMap.servo.get("cls")

    fun rotateToNextArtifact(): Action = controlAction(
        init = {

        },
        update = {
            // Implement rotation logic here
            true // Return true when the action is complete
        },
    )

    override fun update(telemetry: Telemetry) {
        robot
        TODO("Not yet implemented")
    }
}