package dev.kingssack.volt.drivetrain

import com.acmerobotics.roadrunner.Action
import com.acmerobotics.roadrunner.PoseVelocity2d
import com.pedropathing.follower.Follower
import com.pedropathing.follower.FollowerConstants
import com.pedropathing.ftc.FollowerBuilder
import com.pedropathing.ftc.drivetrains.MecanumConstants
import com.pedropathing.ftc.localization.constants.DriveEncoderConstants
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathChain
import com.pedropathing.paths.PathConstraints
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry

class SimpleMecanumDriveWithPP(
    hardwareMap: HardwareMap,
    followerConstants: FollowerConstants,
    localizerConstants: DriveEncoderConstants,
    pathConstraints: PathConstraints,
    driveConstants: MecanumConstants,
    var pose: Pose = Pose(),
) : Drivetrain() {
    private val follower: Follower =
        FollowerBuilder(followerConstants, hardwareMap)
            .driveEncoderLocalizer(localizerConstants)
            .pathConstraints(pathConstraints)
            .mecanumDrivetrain(driveConstants)
            .build()

    init {
        follower.setStartingPose(pose)
        follower.update()
    }

    fun startTeleOpDrive() {
        follower.startTeleOpDrive()
    }

    fun setDrivePowers(powers: PoseVelocity2d) {
        follower.setTeleOpDrive(powers.linearVel.x, powers.linearVel.y, powers.angVel)
    }

    fun pathTo(pathChain: PathChain): Action = Action {
        if (!follower.isBusy) follower.followPath(pathChain)

        follower.update()
        val finished = !follower.isBusy

        finished
    }

    override fun update(telemetry: Telemetry) {
        follower.update()

        telemetry.addLine("DRIVETRAIN-->")
        telemetry.addData("x", follower.pose.x)
        telemetry.addData("y", follower.pose.y)
        telemetry.addData("heading", follower.pose.heading)
        telemetry.addLine()
    }
}
