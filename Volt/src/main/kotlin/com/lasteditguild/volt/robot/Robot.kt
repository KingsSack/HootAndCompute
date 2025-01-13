package com.lasteditguild.volt.robot

import com.acmerobotics.roadrunner.Pose2d
import com.lasteditguild.volt.attachment.Attachment
import com.lasteditguild.volt.util.SimpleMecanumDrive
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry

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
    abstract val drive: SimpleMecanumDrive

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