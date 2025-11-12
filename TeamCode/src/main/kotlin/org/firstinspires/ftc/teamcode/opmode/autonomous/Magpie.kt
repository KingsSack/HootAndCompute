package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.dashboard.config.Config
import com.pedropathing.geometry.BezierLine
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathChain
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import dev.kingssack.volt.opmode.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.robot.Jones
import org.firstinspires.ftc.teamcode.util.toRadians

@Config
@Autonomous(name = "Magpie", group = "Competition")
class Magpie :
    AutonomousMode<Jones>(
        robotFactory = { hardwareMap ->
            Jones(hardwareMap, Pose(INITIAL_X, INITIAL_Y, INITIAL_HEADING.toRadians()))
        }
    ) {
    companion object {
        @JvmField var INITIAL_X: Double = 56.0
        @JvmField var INITIAL_Y: Double = 8.0
        @JvmField var INITIAL_HEADING: Double = 90.0
    }

    val pathToLoadingZone: PathChain by lazy {
        robot.drivetrain.follower
            .pathBuilder()
            .addPath(BezierLine(Pose(56.0, 8.0), Pose(15.0, 8.0)))
            .setLinearHeadingInterpolation(90.0.toRadians(), 90.0.toRadians())
            .build()
    }

    val pathToLaunchZone: PathChain by lazy {
        robot.drivetrain.follower
            .pathBuilder()
            .addPath(BezierLine(Pose(15.0, 8.0), Pose(72.0, 30.0)))
            .setLinearHeadingInterpolation(90.0.toRadians(), 80.0.toRadians())
            .build()
    }

    override fun sequence() = execute {
        +robot.drivetrain.pathTo(pathToLoadingZone)
        +robot.drivetrain.pathTo(pathToLaunchZone)
        +robot.launcher.enable()
        +robot.launcher.disable()
    }
}
