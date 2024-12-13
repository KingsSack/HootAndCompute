package org.firstinspires.ftc.teamcode.rr

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.ftc.runBlocking
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.util.MecanumDrive

@Config
@TeleOp(name = "RoadRunner - Tuning", group = "Test")
class RoadRunnerTuning : LinearOpMode() {
    companion object Config {
        @JvmField
        var initialX: Double = 0.0
        @JvmField
        var initialY: Double = 0.0
        @JvmField
        var initialHeading: Double = 0.0
        @JvmField
        var distance: Double = 32.0
    }

    private lateinit var drive: MecanumDrive

    override fun runOpMode() {
        drive = MecanumDrive(hardwareMap, Pose2d(initialX, initialY, initialHeading))

        waitForStart()

        while (opModeIsActive()) {
            runBlocking(
                drive.actionBuilder(Pose2d(initialX, initialY, initialHeading))
                    .lineToX(initialX + distance)
                    .lineToX(initialX)
                    .build())
        }
    }
}