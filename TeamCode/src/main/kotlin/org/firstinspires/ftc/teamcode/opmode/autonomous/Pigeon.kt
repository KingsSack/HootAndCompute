package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.pedropathing.geometry.Pose
import dev.kingssack.volt.attachment.drivetrain.MecanumDriveWithPP
import dev.kingssack.volt.opmode.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.robot.Gabe
import com.pedropathing.paths.PathChain
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.opmode.autonomous.MultiDualAutonomousMode
import org.firstinspires.ftc.teamcode.robot.GabePP
import org.firstinspires.ftc.teamcode.util.StartingPosition
import org.firstinspires.ftc.teamcode.util.toRadians

@Suppress("unused")
@VoltOpModeMeta("Pigeon", "Competition", "Manatee")
abstract class Pigeon :
    MultiDualAutonomousMode<GabePP, StartingPosition>(StartingPosition::class.java) {

    private val endPose: Pose = when (type) {
        StartingPosition.WALL -> Pose(37.0, 9.0, 90.0.toRadians()).maybeFlip(alliance)
        StartingPosition.GOAL -> Pose(36.0, 125.0, 323.0.toRadians()).maybeFlip(alliance)
        StartingPosition.RAMP -> Pose(15.0, 105.0, 0.0.toRadians()).maybeFlip(alliance)
    }
    override val robot: GabePP = GabePP(hardwareMap, sw(
            when (type) {
                StartingPosition.WALL -> Pose(57.0, 9.0, 90.0.toRadians())
                StartingPosition.GOAL -> Pose(26.0, 133.0, 323.0.toRadians())
                StartingPosition.RAMP -> Pose(15.0, 112.0, 0.0.toRadians())
            }))

    init {
        blackboard["allianceColor"] = color
    }

    /** Drives off the launch line and saves pose */
    override fun sequence() = execute {
        with(robot) {
            +drivetrain.path { lineTo(endPose) }
            instant { blackboard["endPose"] = drivetrain.pose }
        }
    }
}
