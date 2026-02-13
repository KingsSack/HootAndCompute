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

abstract class Pigeon(
    private val alliance: AllianceColor,
    private val initialPose: Pose,
    private val startingPosition: StartingPosition,
) : AutonomousMode<Gabe<MecanumDriveWithPP>>({ GabePP(it, initialPose) }) {
    private lateinit var endPose: Pose

    // Leaves the launch line
    override val sequence = sequence {
        +robot.drivetrain.path { lineTo(endPose) }
        instant { blackboard["endPose"] = robot.drivetrain.pose }
    }

    override fun initialize() {
        super.initialize()
        endPose =
            when (startingPosition) {
                StartingPosition.WALL -> Pose(37.0, 9.0, 90.0.toRadians()).maybeFlip(alliance)
                StartingPosition.GOAL -> Pose(36.0, 125.0, 323.0.toRadians()).maybeFlip(alliance)
                StartingPosition.RAMP -> Pose(15.0, 105.0, 0.0.toRadians()).maybeFlip(alliance)
            }
        blackboard["allianceColor"] = alliance
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
