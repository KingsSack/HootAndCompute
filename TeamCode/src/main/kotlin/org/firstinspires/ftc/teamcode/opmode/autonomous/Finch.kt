package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.dashboard.config.Config
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathChain
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.teamcode.robot.GabePP
import org.firstinspires.ftc.teamcode.robot.drivetrain
import org.firstinspires.ftc.teamcode.robot.launcher
import org.firstinspires.ftc.teamcode.robot.storage
import dev.kingssack.volt.opmode.autonomous.MultiDualAutonomousMode
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
    }

    override fun sequence() = execute {
        parallel {
            storage { +close() }
            drivetrain { +pathTo(pathToLaunchZone) }
        }

        launcher { +enable() }
        repeat(3) {
            storage {
                wait(3.0)
                +release()
                wait(0.6)
                +close()
            }
        }
        wait(4.0)
        launcher { +disable() }
    }
}
enum class FinchStartingPosition {
    WALL,
    GOAL
}
