package dev.kingssack.volt.attachment.drivetrain

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

/**
 * A mecanum drivetrain integrated with the PedroPathing library.
 *
 * @param hardwareMap The FTC hardware map.
 * @param followerConstants Constants for the path follower.
 * @param localizerConstants Constants for the drive encoder localizer.
 * @param pathConstraints Constraints for path following.
 * @param driveConstants Constants specific to the mecanum drivetrain.
 * @property pose The initial pose of the robot.
 * @property follower The path follower instance.
 */
class MecanumDriveWithPP(
    hardwareMap: HardwareMap,
    followerConstants: FollowerConstants,
    localizerConstants: DriveEncoderConstants,
    pathConstraints: PathConstraints,
    driveConstants: MecanumConstants,
    var pose: Pose = Pose(),
) : MecanumDrivetrain() {
    val follower: Follower =
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

    override fun setDrivePowers(powers: PoseVelocity2d) {
        follower.setTeleOpDrive(powers.linearVel.x, powers.linearVel.y, powers.angVel)
    }

    fun pathTo(pathChain: PathChain): Action = Action {
        follower.followPath(pathChain)

        follower.update()
        val finished = !follower.isBusy

        !finished
    }

    context(telemetry: Telemetry)
    override fun update() {
        follower.update()

        super.update()
        telemetry.addData("x", follower.pose.x)
        telemetry.addData("y", follower.pose.y)
        telemetry.addData("heading", follower.pose.heading)
        telemetry.addLine()
    }
}
