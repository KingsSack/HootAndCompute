package org.firstinspires.ftc.teamcode.robot

import com.acmerobotics.roadrunner.Pose2d
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.attachment.Attachment
import org.firstinspires.ftc.teamcode.util.MecanumDrive

/**
 * Represents a robot.
 *
 * @param hardwareMap for initializing hardware components
 * @param initialPose of the robot
 */
abstract class Robot(hardwareMap: HardwareMap, initialPose: Pose2d) {
    // Attachments
    protected var attachments: List<Attachment> = listOf()

    // The mecanum drive of the robot.
    val drive = MecanumDrive(hardwareMap, initialPose)

    /**
     * Updates the robot.
     *
     * @param telemetry for updating telemetry
     */
    fun update(telemetry: Telemetry) {
        drive.updatePoseEstimate()
        attachments.forEach { it.update(telemetry) }

        // Update telemetry
        telemetry.update()
    }
}