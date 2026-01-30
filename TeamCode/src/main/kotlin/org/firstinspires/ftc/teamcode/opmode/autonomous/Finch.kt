package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.InstantAction
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathChain
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.opmode.autonomous.MultiDualAutonomousMode
import org.firstinspires.ftc.teamcode.robot.GabePP
import org.firstinspires.ftc.teamcode.util.PathConstants
import org.firstinspires.ftc.teamcode.util.toRadians

@Suppress("unused")
@Config
abstract class Finch() :
    MultiDualAutonomousMode<GabePP, FinchStartingPosition>(FinchStartingPosition::class.java) {
    override val name = "Finch"
    override val autoTransition = "Manatee"
    override fun getRobot(hardwareMap: HardwareMap): GabePP {
        return GabePP(hardwareMap, sw(if (type == FinchStartingPosition.WALL) Pose(56.0, 9.0, 90.0.toRadians()) else Pose(26.0, 133.0, 142.0.toRadians())))
    }
    private val paths by lazy { PathConstants(robot.drivetrain.follower, color) }
    private lateinit var pathToLaunchZone: PathChain

    override fun initialize() {
        super.initialize()
        pathToLaunchZone =
            when (type) {
                FinchStartingPosition.WALL -> paths.pathToLaunchZoneFromWall
                FinchStartingPosition.GOAL -> paths.pathToLaunchZoneFromGoal
            }
        blackboard["allianceColor"] = color
    }

    /** Drives to launch zone, fires, and saves pose */
    override fun sequence() = execute {
        with(robot) {
            parallel {
                +storage.close()
                +drivetrain.pathTo(pathToLaunchZone)
            }

            +fire(3)
            +InstantAction { blackboard["endPose"] = drivetrain.pose }
        }
    }
}
enum class FinchStartingPosition {
    WALL,
    GOAL
}
