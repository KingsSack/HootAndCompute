package org.firstinspires.ftc.teamcode.util

import com.pedropathing.follower.Follower
import com.pedropathing.geometry.BezierLine
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathChain

class PathConstants(follower: Follower, alliance: AllianceColor) {
    private fun Pose.maybeFlip(alliance: AllianceColor): Pose =
        when (alliance) {
            AllianceColor.BLUE -> this.mirror()
            AllianceColor.RED -> this
        }

    val pathToLoadingZone: PathChain =
        follower
            .pathBuilder()
            .addPath(BezierLine(Pose(56.0, 8.0).maybeFlip(alliance), Pose(15.0, 8.0).maybeFlip(alliance)))
            .setLinearHeadingInterpolation(90.0.toRadians(), 90.0.toRadians())
            .build()

    val pathToLaunchZone: PathChain =
        follower
            .pathBuilder()
            .addPath(BezierLine(Pose(15.0, 8.0).maybeFlip(alliance), Pose(72.0, 30.0).maybeFlip(alliance)))
            .setLinearHeadingInterpolation(90.0.toRadians(), 80.0.toRadians())
            .build()

    val pathToLaunchZoneT: PathChain =
        follower
            .pathBuilder()
            .addPath(BezierLine(Pose(22.0, 121.0).maybeFlip(alliance), Pose(53.0, 91.0).maybeFlip(alliance)))
            .setLinearHeadingInterpolation(315.0.toRadians(), 135.0.toRadians())
            .build()

    val pathToLaunchZoneB: PathChain =
        follower
            .pathBuilder()
            .addPath(BezierLine(Pose(56.0, 8.0).maybeFlip(alliance), Pose(53.0, 91.0).maybeFlip(alliance)))
            .setLinearHeadingInterpolation(90.0.toRadians(), 135.0.toRadians())
            .build()
}
