package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathChain
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import dev.kingssack.volt.opmode.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.robot.GabePP
import org.firstinspires.ftc.teamcode.util.AllianceColor
import org.firstinspires.ftc.teamcode.util.PathConstants
import org.firstinspires.ftc.teamcode.util.StartingPosition

abstract class Pigeon(
    alliance: AllianceColor,
    initialPose: Pose,
    private val startingPosition: StartingPosition,
) : AutonomousMode<GabePP>({ hardwareMap -> GabePP(hardwareMap, initialPose) }) {
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
    }

    override fun sequence() = execute { +robot.drivetrain.pathTo(pathOffLaunchLine) }
}

@Autonomous(name = "Pigeon Wall Blue", group = "Competition", preselectTeleOp = "Manatee")
class PigeonWallBlue : Pigeon(AllianceColor.BLUE, Pose(57.0, 9.0, 90.0), StartingPosition.WALL)

@Autonomous(name = "Pigeon Wall Red", group = "Competition", preselectTeleOp = "Manatee")
class PigeonWallRed :
    Pigeon(AllianceColor.RED, Pose(57.0, 9.0, 90.0).mirror(), StartingPosition.WALL)

@Autonomous(name = "Pigeon Goal Blue", group = "Competition", preselectTeleOp = "Manatee")
class PigeonGoalBlue : Pigeon(AllianceColor.BLUE, Pose(26.0, 133.0, 323.0), StartingPosition.GOAL)

@Autonomous(name = "Pigeon Goal Red", group = "Competition", preselectTeleOp = "Manatee")
class PigeonGoalRed :
    Pigeon(AllianceColor.RED, Pose(26.0, 133.0, 323.0).mirror(), StartingPosition.GOAL)

@Autonomous(name = "Pigeon Ramp Blue", group = "Competition", preselectTeleOp = "Manatee")
class PigeonRampBlue : Pigeon(AllianceColor.BLUE, Pose(15.0, 112.0, 0.0), StartingPosition.RAMP)

@Autonomous(name = "Pigeon Ramp Red", group = "Competition", preselectTeleOp = "Manatee")
class PigeonRampRed :
    Pigeon(AllianceColor.RED, Pose(15.0, 112.0, 0.0).mirror(), StartingPosition.RAMP)
