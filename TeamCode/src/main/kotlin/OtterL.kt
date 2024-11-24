package org.firstinspires.ftc.teamcode

import autonomous.Auto
import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import robot.Steve

@Autonomous(name = "Otter - Left", group = "Competition")
class OtterL : LinearOpMode() {
    // Robot
    private val robot = Steve()

    // Initial position
    private val initialPose = Pose2d(Vector2d(0.0, 0.0), 90.0)

    // Sample positions
    private val samplePoses: List<Pose2d> = listOf(
        Pose2d(Vector2d(609.6, 609.6), 90.0)  // First sample to collect
    )

    // Basket position
    private val basketPose = Pose2d(Vector2d(-304.8, 0.0), -45.0)

    // Observation zone position
    private val observationZonePosition = Pose2d(Vector2d(-609.6, -2133.6), 90.0)

    // Autonomous script
    private val auto = Auto(robot, false, samplePoses, basketPose, observationZonePosition)

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