package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathChain
import dev.kingssack.volt.opmode.autonomous.DualAutonomousMode
import org.firstinspires.ftc.teamcode.robot.GabePP
import dev.kingssack.volt.opmode.autonomous.AllianceColor
import org.firstinspires.ftc.teamcode.util.PathConstants
import org.firstinspires.ftc.teamcode.util.StartingPosition

@Suppress("unused")
abstract class Pigeon(
    color: AllianceColor,
    initialPose: Pose,
    private val startingPosition: StartingPosition,
) : DualAutonomousMode<GabePP>({ hardwareMap -> GabePP(hardwareMap,  if (color == AllianceColor.BLUE) initialPose else initialPose.mirror()) }) {

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

class PigeonWallBlue(
    color: AllianceColor
) : Pigeon(color, Pose(57.0, 9.0, 90.0), StartingPosition.WALL)

class PigeonGoalBlue(
    color: AllianceColor
) : Pigeon(color, Pose(26.0, 133.0, 323.0), StartingPosition.GOAL)

class PigeonRampBlue(
    color: AllianceColor
) : Pigeon(color, Pose(15.0, 112.0, 0.0), StartingPosition.RAMP)
