package org.firstinspires.ftc.teamcode

import org.firstinspires.ftc.teamcode.autonomous.Auto
import com.acmerobotics.roadrunner.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode

@Autonomous(name = "Otter - Test", group = "Test", preselectTeleOp = "Whale")
class Test : LinearOpMode() {
    // Initial position
    private val initialPose = Pose2d(-24.0, 60.0, Math.toRadians(-90.0))

    // Sample positions
    private val samplePoses: List<Pose2d> = listOf() // No samples to collect

    // Basket position
    private val basketPose = Pose2d(60.0, 60.0, Math.toRadians(45.0))

    // Observation zone position
    private val observationZonePose = Pose2d(-60.0, 60.0, Math.toRadians(-90.0))

    // Autonomous script
    private lateinit var auto: Auto

    override fun runOpMode() {
        // Initialize
        auto = Auto(hardwareMap, telemetry, initialPose, false, samplePoses, basketPose, observationZonePose)

        // Wait for start
        waitForStart()

        // Loop
        if (opModeIsActive())
            auto.test()
    }
}