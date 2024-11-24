package org.firstinspires.ftc.teamcode

import autonomous.Auto
import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import robot.Steve

@Autonomous(name = "Otter - Right", group = "Competition")
class OtterR : LinearOpMode() {
    // Robot
    private val robot = Steve()

    // Initial position
    private val initialPose = Pose2d(0.0, 0.0, 90.0)

    // Sample positions
    private val samplePoses: List<Pose2d> = listOf() // No samples to collect

    // Basket position
    private val basketPose = Pose2d(Vector2d(-304.8, 0.0), -45.0)

    // Observation zone position
    private val observationZonePose = Pose2d(Vector2d(0.0, 609.6), 90.0)

    // Autonomous script
    private val auto = Auto(robot, false, samplePoses, basketPose, observationZonePose)

    override fun runOpMode() {
        // Initialize
        robot.init(hardwareMap)
        auto.init(hardwareMap, telemetry, initialPose)

        // Wait for start
        waitForStart()

        // Loop
        while (opModeIsActive()) {
            auto.tick(telemetry)
        }
    }
}