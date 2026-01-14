package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.roadrunner.InstantAction
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathChain
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import dev.kingssack.volt.opmode.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.robot.GabePP
import org.firstinspires.ftc.teamcode.util.AllianceColor
import org.firstinspires.ftc.teamcode.util.PathConstants
import org.firstinspires.ftc.teamcode.util.StartingPosition
import org.firstinspires.ftc.teamcode.util.toRadians

abstract class Pigeon(
    private val alliance: AllianceColor,
    initialPose: Pose,
    private val startingPosition: StartingPosition,
) : AutonomousMode<GabePP>({ GabePP(it, initialPose, alliance) }) {
    private val paths by lazy { PathConstants(robot.drivetrain.follower, alliance) }

    private lateinit var pathOffLaunchLine: PathChain

    override fun initialize() {
        super.initialize()
        pathOffLaunchLine =
            when (startingPosition) {
                StartingPosition.WALL -> paths.pathOffWallLaunchLine
                StartingPosition.GOAL -> paths.pathOffGoalLaunchLine
                StartingPosition.RAMP -> paths.pathOffRampLaunchLine
            }
        blackboard["allianceColor"] = alliance
    }

    /** Drives off the launch line and saves pose */
    override fun sequence() = execute {
        with(robot) {
            +drivetrain.pathTo(pathOffLaunchLine)
            +InstantAction { blackboard["endPose"] = drivetrain.pose }
        }
    }
}

@Autonomous(name = "Pigeon Wall Blue", group = "Competition", preselectTeleOp = "Manatee")
class PigeonWallBlue :
    Pigeon(AllianceColor.BLUE, Pose(57.0, 9.0, 90.0.toRadians()), StartingPosition.WALL)

@Autonomous(name = "Pigeon Wall Red", group = "Competition", preselectTeleOp = "Manatee")
class PigeonWallRed :
    Pigeon(AllianceColor.RED, Pose(57.0, 9.0, 90.0.toRadians()).mirror(), StartingPosition.WALL)

@Autonomous(name = "Pigeon Goal Blue", group = "Competition", preselectTeleOp = "Manatee")
class PigeonGoalBlue :
    Pigeon(AllianceColor.BLUE, Pose(26.0, 133.0, 323.0.toRadians()), StartingPosition.GOAL)

@Autonomous(name = "Pigeon Goal Red", group = "Competition", preselectTeleOp = "Manatee")
class PigeonGoalRed :
    Pigeon(AllianceColor.RED, Pose(26.0, 133.0, 323.0.toRadians()).mirror(), StartingPosition.GOAL)

@Autonomous(name = "Pigeon Ramp Blue", group = "Competition", preselectTeleOp = "Manatee")
class PigeonRampBlue :
    Pigeon(AllianceColor.BLUE, Pose(15.0, 112.0, 0.0.toRadians()), StartingPosition.RAMP)

@Autonomous(name = "Pigeon Ramp Red", group = "Competition", preselectTeleOp = "Manatee")
class PigeonRampRed :
    Pigeon(AllianceColor.RED, Pose(15.0, 112.0, 0.0.toRadians()).mirror(), StartingPosition.RAMP)
