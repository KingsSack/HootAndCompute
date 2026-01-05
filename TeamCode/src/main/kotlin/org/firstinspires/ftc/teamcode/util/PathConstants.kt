package org.firstinspires.ftc.teamcode.util
import dev.kingssack.volt.opmode.autonomous.AllianceColor
import com.pedropathing.follower.Follower
import com.pedropathing.geometry.BezierLine
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathChain

class PathConstants(follower: Follower, alliance: AllianceColor) {
    private fun Pose.maybeFlip(alliance: AllianceColor): Pose =
        when (alliance) {
            AllianceColor.BLUE -> this
            AllianceColor.RED -> this.mirror()
        }

    val pathOffWallLaunchLine: PathChain =
        follower
            .pathBuilder()
            .addPath(BezierLine(Pose(57.0, 9.0).maybeFlip(alliance), Pose(37.0, 9.0).maybeFlip(alliance)))
            .setLinearHeadingInterpolation(90.0.toRadians(), 90.0.toRadians())
            .build()

    val pathOffGoalLaunchLine: PathChain =
        follower
            .pathBuilder()
            .addPath(BezierLine(Pose(26.0, 133.0).maybeFlip(alliance), Pose(36.0, 125.0).maybeFlip(alliance)))
            .setLinearHeadingInterpolation(323.0.toRadians(), 323.0.toRadians())
            .build()

    val pathOffRampLaunchLine: PathChain =
        follower
            .pathBuilder()
            .addPath(BezierLine(Pose(15.0, 112.0).maybeFlip(alliance), Pose(15.0, 105.0).maybeFlip(alliance)))
            .setLinearHeadingInterpolation(0.0.toRadians(), 0.0.toRadians())
            .build()

    val pathToLaunchZoneFromGoal: PathChain =
        follower
            .pathBuilder()
            .addPath(BezierLine(Pose(26.0, 132.0).maybeFlip(alliance), Pose(64.0, 125.0).maybeFlip(alliance)))
            .setLinearHeadingInterpolation(142.0.toRadians(), if (alliance == AllianceColor.BLUE) 148.0.toRadians() else 136.0.toRadians())
            .build()

    val pathToLaunchZoneFromWall: PathChain =
        follower
            .pathBuilder()
            .addPath(BezierLine(Pose(56.0, 9.0).maybeFlip(alliance), Pose(64.0, 100.0).maybeFlip(alliance)))
            .setLinearHeadingInterpolation(90.0.toRadians(), if (alliance == AllianceColor.BLUE) 140.0.toRadians() else 40.0.toRadians())
            .build()
}
