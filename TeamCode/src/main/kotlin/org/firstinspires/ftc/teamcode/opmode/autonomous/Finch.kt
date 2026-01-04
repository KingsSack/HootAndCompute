package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.InstantAction
import com.pedropathing.geometry.Pose
import org.firstinspires.ftc.teamcode.robot.GabePP
import org.firstinspires.ftc.teamcode.robot.drivetrain
import org.firstinspires.ftc.teamcode.robot.launcher
import org.firstinspires.ftc.teamcode.robot.storage
import dev.kingssack.volt.opmode.autonomous.DualAutonomousMode
import org.firstinspires.ftc.teamcode.util.PathConstants
import org.firstinspires.ftc.teamcode.util.toRadians

@Config
@Suppress("unused")
class Finch :
    DualAutonomousMode<GabePP>({ hardwareMap ->
        GabePP(hardwareMap, Pose(INITIAL_X, INITIAL_Y, INITIAL_HEADING.toRadians()).mirror())
    }) {
    companion object {
        @JvmField var INITIAL_X: Double = 56.0
        @JvmField var INITIAL_Y: Double = 8.0
        @JvmField var INITIAL_HEADING: Double = 90.0
    }
    override val name = "Finch"
    override val autoTransition = "Manatee"
    private val paths by lazy { PathConstants(robot.drivetrain.follower, color) }

    override fun sequence() = execute {
        drivetrain {
            +pathTo(paths.pathToLaunchZoneB)
        }
        launcher {
            +enable()
        }
        repeat(3) {
            storage {
                +release()
                +InstantAction { sleep(500) }
                +close()
                +InstantAction { sleep(5000) }
            }
        }
        launcher {
            +disable()
        }
    }
}