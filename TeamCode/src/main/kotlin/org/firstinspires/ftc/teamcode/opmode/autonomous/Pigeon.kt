package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathChain
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.opmode.autonomous.DualAutonomousMode
import org.firstinspires.ftc.teamcode.robot.GabePP
import org.firstinspires.ftc.teamcode.util.PathConstants
import org.firstinspires.ftc.teamcode.util.StartingPosition

@Suppress("unused")
abstract class Pigeon(
    val initialPose: Pose,
    private val startingPosition: StartingPosition,
) : DualAutonomousMode<GabePP>() {

    override fun getRobot(hardwareMap: HardwareMap): GabePP {
        return GabePP(hardwareMap, sw(initialPose, initialPose.mirror()))
    }

    override val name = "Pigeon " + if (startingPosition == StartingPosition.WALL) "wall" else if (startingPosition == StartingPosition.GOAL) "goal" else "ramp"
    override val group = "Competition"
    override val autoTransition = "Manatee"
    private val paths by lazy { PathConstants(robot.drivetrain.follower, color) }
    private lateinit var pathOffLaunchLine: PathChain

    override fun initialize() {
        super.initialize()
        pathOffLaunchLine =
            when (startingPosition) {
                StartingPosition.WALL -> paths.pathOffWallLaunchLine
                StartingPosition.GOAL -> paths.pathOffGoalLaunchLine
                StartingPosition.RAMP -> paths.pathOffRampLaunchLine
            }
    }

    override fun sequence() = execute { +robot.drivetrain.pathTo(pathOffLaunchLine) }
}

@Suppress("unused")
class PigeonWallBlue : Pigeon(Pose(57.0, 9.0, 90.0), StartingPosition.WALL)

@Suppress("unused")
class PigeonGoalBlue : Pigeon(Pose(26.0, 133.0, 323.0), StartingPosition.GOAL)

@Suppress("unused")
class PigeonRampBlue : Pigeon(Pose(15.0, 112.0, 0.0), StartingPosition.RAMP)
