package org.firstinspires.ftc.teamcode

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.PoseVelocity2d
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.teamcode.util.Drawing.drawRobot
import org.firstinspires.ftc.teamcode.util.MecanumDrive


@TeleOp(name = "RoadRunner - Test", group = "Test")
class RoadRunnerTest : LinearOpMode() {
    private lateinit var drive: MecanumDrive

    override fun runOpMode() {
        registerDrive(hardwareMap, Pose2d(Vector2d(Configuration.testParams.initialX, Configuration.testParams.initialY), Configuration.testParams.initialHeading))

        waitForStart()

        while (opModeIsActive()) {
            drive.setDrivePowers(
                PoseVelocity2d(
                    Vector2d(
                        -gamepad1.left_stick_y.toDouble(),
                        -gamepad1.left_stick_x.toDouble()
                    ),
                    -gamepad1.right_stick_x.toDouble()
                )
            )

            drive.updatePoseEstimate()

            telemetry.addData("x", drive.pose.position.x)
            telemetry.addData("y", drive.pose.position.y)
            telemetry.addData("heading (deg)", Math.toDegrees(drive.pose.heading.toDouble()))
            telemetry.update()

            val packet = TelemetryPacket()
            packet.fieldOverlay().setStroke("#3F51B5")
            drawRobot(packet.fieldOverlay(), drive.pose)
            FtcDashboard.getInstance().sendTelemetryPacket(packet)
        }
    }

    private fun registerDrive(hardwareMap: HardwareMap, initialPose: Pose2d) {
        drive = MecanumDrive(hardwareMap, initialPose)
    }
}