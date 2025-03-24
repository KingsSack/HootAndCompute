package org.firstinspires.ftc.teamcode.robot

import com.acmerobotics.roadrunner.Action
import com.qualcomm.hardware.dfrobot.HuskyLens
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.attachment.SimpleAttachmentWithDcMotor
import dev.kingssack.volt.robot.Robot
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.attachment.Example

class ExampleRobot(hardwareMap: HardwareMap) : Robot() {
    // Sensors
    private val huskyLens: HuskyLens = hardwareMap.get(HuskyLens::class.java, "lens")

    // Attachments
    val exampleAttachment: SimpleAttachmentWithDcMotor = SimpleAttachmentWithDcMotor(hardwareMap, "motor", 0.5, 1000)
    val exampleAttachmentTwo: SimpleAttachmentWithDcMotor = SimpleAttachmentWithDcMotor(hardwareMap, "motor", 0.5, 1000)

    val example: Example = Example(hardwareMap, "example1", "example2")

    init {
        attachments = listOf(exampleAttachment, exampleAttachmentTwo)

        // Set huskylens mode
        huskyLens.selectAlgorithm(HuskyLens.Algorithm.OBJECT_RECOGNITION)
    }

    fun slap(): Action {
        // Move motor to 1000 at 50% power
        return exampleAttachment.goTo(0.5, 1000)
    }

    fun getDetectedObjects(telemetry: Telemetry): Array<out HuskyLens.Block>? {
        // Get objects
        val blocks = huskyLens.blocks()
        telemetry.addData("Block count", blocks.size)
        for (block in blocks) {
            telemetry.addData("Block", block.toString())
        }
        return blocks
    }
}