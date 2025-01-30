package dev.kingssack.volt.robot

import dev.kingssack.volt.attachment.Attachment
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * Represents a robot with attachments.
 */
abstract class Robot {
    // Attachments
    protected var attachments: List<Attachment> = listOf()

    /**
     * Updates the robot.
     *
     * @param telemetry for updating telemetry
     */
    open fun update(telemetry: Telemetry) {
        attachments.forEach { it.update(telemetry) }

        // Update telemetry
        telemetry.update()
    }
}