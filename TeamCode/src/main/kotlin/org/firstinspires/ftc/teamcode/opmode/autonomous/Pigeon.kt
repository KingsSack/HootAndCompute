package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.roadrunner.InstantAction
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathChain
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.opmode.autonomous.MultiDualAutonomousMode
import org.firstinspires.ftc.teamcode.robot.GabePP
import org.firstinspires.ftc.teamcode.util.PathConstants
import org.firstinspires.ftc.teamcode.util.StartingPosition
import org.firstinspires.ftc.teamcode.util.toRadians

@Suppress("unused")
abstract class Pigeon() :
    MultiDualAutonomousMode<GabePP, StartingPosition>(StartingPosition::class.java) {

    override fun getRobot(hardwareMap: HardwareMap): GabePP {
        return GabePP(hardwareMap, sw(
            when (type) {
                StartingPosition.WALL -> Pose(57.0, 9.0, 90.0.toRadians())
                StartingPosition.GOAL -> Pose(26.0, 133.0, 323.0.toRadians())
                StartingPosition.RAMP -> Pose(15.0, 112.0, 0.0.toRadians())
            }))
    }

    override val name = "Pigeon"
    override val group = "Competition"
    override val autoTransition = "Manatee"
    private val paths by lazy { PathConstants(robot.drivetrain.follower, color) }
    private lateinit var pathOffLaunchLine: PathChain

    override fun initialize() {
        super.initialize()
        pathOffLaunchLine =
            when (type) {
                StartingPosition.WALL -> paths.pathOffWallLaunchLine
                StartingPosition.GOAL -> paths.pathOffGoalLaunchLine
                StartingPosition.RAMP -> paths.pathOffRampLaunchLine
            }
        blackboard["allianceColor"] = color
    }

    /** Drives off the launch line and saves pose */
    override fun sequence() = execute {
        with(robot) {
            +drivetrain.pathTo(pathOffLaunchLine)
            +InstantAction { blackboard["endPose"] = drivetrain.pose }
        }
    }
}
