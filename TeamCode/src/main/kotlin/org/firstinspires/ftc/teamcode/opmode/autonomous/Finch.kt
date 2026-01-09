package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.dashboard.config.Config
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathChain
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import dev.kingssack.volt.opmode.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.robot.GabePP
import org.firstinspires.ftc.teamcode.robot.drivetrain
import org.firstinspires.ftc.teamcode.robot.launcher
import org.firstinspires.ftc.teamcode.robot.storage
import org.firstinspires.ftc.teamcode.util.AllianceColor
import org.firstinspires.ftc.teamcode.util.PathConstants
import org.firstinspires.ftc.teamcode.util.StartingPosition
import org.firstinspires.ftc.teamcode.util.toRadians

@Config
abstract class Finch(
    alliance: AllianceColor,
    initialPose: Pose,
    private val startingPosition: StartingPosition,
) : AutonomousMode<GabePP>({ hardwareMap -> GabePP(hardwareMap, initialPose) }) {
    private val paths by lazy { PathConstants(robot.drivetrain.follower, alliance) }

    private lateinit var pathToLaunchZone: PathChain

    override fun initialize() {
        super.initialize()
        pathToLaunchZone =
            when (startingPosition) {
                StartingPosition.WALL -> paths.pathToLaunchZoneFromWall
                StartingPosition.GOAL -> paths.pathToLaunchZoneFromGoal
                StartingPosition.RAMP -> TODO("RAMP starting position not implemented")
            }
    }

    override fun sequence() = execute {
        parallel {
            storage { +close() }
            drivetrain { +pathTo(pathToLaunchZone) }
        }

        launcher { +enable() }
        storage {
            wait(0.5)
            +release()
            wait(10.0)
            +close()
        }
        wait(4.0)
        launcher { +disable() }
    }
}

@Autonomous(name = "Finch Wall Blue", group = "Competition", preselectTeleOp = "Manatee")
class FinchWallBlue :
    Finch(AllianceColor.BLUE, Pose(56.0, 9.0, 90.0.toRadians()), StartingPosition.WALL)

@Autonomous(name = "Finch Wall Red", group = "Competition", preselectTeleOp = "Manatee")
class FinchWallRed :
    Finch(AllianceColor.RED, Pose(56.0, 9.0, 90.0.toRadians()).mirror(), StartingPosition.WALL)

@Autonomous(name = "Finch Goal Blue", group = "Competition", preselectTeleOp = "Manatee")
class FinchGoalBlue :
    Finch(AllianceColor.BLUE, Pose(26.0, 133.0, 142.0.toRadians()), StartingPosition.GOAL)

@Autonomous(name = "Finch Goal Red", group = "Competition", preselectTeleOp = "Manatee")
class FinchGoalRed :
    Finch(AllianceColor.RED, Pose(26.0, 133.0, 142.0.toRadians()).mirror(), StartingPosition.GOAL)
