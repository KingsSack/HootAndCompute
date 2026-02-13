package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import dev.kingssack.volt.attachment.drivetrain.MecanumDriveWithPP
import dev.kingssack.volt.opmode.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.robot.Gabe
import org.firstinspires.ftc.teamcode.robot.GabePP
import org.firstinspires.ftc.teamcode.util.AllianceColor
import org.firstinspires.ftc.teamcode.util.StartingPosition
import org.firstinspires.ftc.teamcode.util.maybeFlip
import org.firstinspires.ftc.teamcode.util.toRadians

abstract class Finch(
    private val alliance: AllianceColor,
    private val initialPose: Pose,
    private val startingPosition: StartingPosition,
) : AutonomousMode<Gabe<MecanumDriveWithPP>>({ GabePP(it, initialPose) }) {
    private lateinit var launchPose: Pose

    // Drives to the launch zone and fires preloaded artifacts
    override val sequence = sequence {
        +robot.drivetrain.path { lineTo(launchPose) }
        +robot.fire(3)
        instant { blackboard["endPose"] = robot.drivetrain.pose }
    }

    override fun initialize() {
        super.initialize()
        launchPose =
            when (startingPosition) {
                StartingPosition.WALL -> Pose(64.0, 100.0, 140.0.toRadians()).maybeFlip(alliance)
                StartingPosition.GOAL -> Pose(64.0, 125.0, 148.0.toRadians()).maybeFlip(alliance)
                StartingPosition.RAMP -> TODO("RAMP starting position not implemented")
            }
        blackboard["allianceColor"] = alliance
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
