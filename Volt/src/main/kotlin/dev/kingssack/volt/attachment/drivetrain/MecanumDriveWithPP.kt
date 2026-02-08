package dev.kingssack.volt.attachment.drivetrain

import com.acmerobotics.roadrunner.Action
import com.acmerobotics.roadrunner.PoseVelocity2d
import com.pedropathing.follower.Follower
import com.pedropathing.follower.FollowerConstants
import com.pedropathing.ftc.FollowerBuilder
import com.pedropathing.ftc.drivetrains.MecanumConstants
import com.pedropathing.ftc.localization.constants.DriveEncoderConstants
import com.pedropathing.geometry.BezierCurve
import com.pedropathing.geometry.BezierLine
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathChain
import com.pedropathing.paths.PathConstraints
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.annotations.VoltAction
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * A mecanum drivetrain integrated with the PedroPathing library
 *
 * @param hardwareMap The FTC hardware map
 * @param followerConstants Constants for the path follower
 * @param localizerConstants Constants for the drive encoder localizer
 * @param pathConstraints Constraints for path following
 * @param driveConstants Constants specific to the mecanum drivetrain
 * @param initialPose The initial pose of the robot
 * @property pose The robot's current pose
 * @property follower The path follower instance
 */
class MecanumDriveWithPP(
    hardwareMap: HardwareMap,
    followerConstants: FollowerConstants,
    localizerConstants: DriveEncoderConstants,
    pathConstraints: PathConstraints,
    driveConstants: MecanumConstants,
    initialPose: Pose = Pose(),
) : MecanumDrivetrain() {
    val follower: Follower =
        FollowerBuilder(followerConstants, hardwareMap)
            .driveEncoderLocalizer(localizerConstants)
            .pathConstraints(pathConstraints)
            .mecanumDrivetrain(driveConstants)
            .build()

    val pose: Pose
        get() = follower.pose

    init {
        follower.setStartingPose(initialPose)
        follower.update()
    }

    fun startTeleOpDrive() {
        follower.startTeleOpDrive()
    }

    override fun setDrivePowers(powers: PoseVelocity2d) {
        follower.setTeleOpDrive(powers.linearVel.x, powers.linearVel.y, powers.angVel)
    }

    /**
     * Follows the given path using PedroPathing
     *
     * @param pathChain The path chain that defines the sequence of poses and waypoints to follow
     * @return An action that executes the path following behavior
     */
    @VoltAction(name = "Follow Path", description = "Follows the given path using PedroPathing")
    fun pathTo(pathChain: PathChain): Action = Action {
        if (!follower.isBusy) follower.followPath(pathChain, true)
        follower.update()
        follower.isBusy
    }

    inner class FollowerActionBuilder(startPose: Pose) {
        private val builder = follower.pathBuilder()
        private var lastPose = startPose

        fun lineTo(endPose: Pose): FollowerActionBuilder {
            builder.addPath(BezierLine(lastPose, endPose))
            builder.setLinearHeadingInterpolation(lastPose.heading, endPose.heading)
            lastPose = endPose
            return this
        }

        fun splineTo(endPose: Pose, controlPose1: Pose, controlPose2: Pose): FollowerActionBuilder {
            builder.addPath(BezierCurve(lastPose, controlPose1, controlPose2, endPose))
            builder.setLinearHeadingInterpolation(lastPose.heading, endPose.heading)
            lastPose = endPose
            return this
        }

        fun build(): Action {
            return pathTo(builder.build())
        }
    }

    /**
     * Creates a path following action starting from the given pose
     *
     * @param startPose The starting pose of the robot
     * @return An [Action] that executes a defined path following behavior
     */
    fun path(
        startPose: Pose = pose,
        block: FollowerActionBuilder.() -> FollowerActionBuilder,
    ): Action {
        return FollowerActionBuilder(startPose).block().build()
    }

    context(telemetry: Telemetry)
    override fun update() {
        follower.update()

        super.update()
        with(telemetry) {
            addData("x", follower.pose.x)
            addData("y", follower.pose.y)
            addData("heading (deg)", Math.toDegrees(follower.pose.heading))
        }
    }
}
