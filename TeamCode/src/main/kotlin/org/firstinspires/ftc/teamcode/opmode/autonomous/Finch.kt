package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.InstantAction
import com.acmerobotics.roadrunner.SequentialAction
import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import dev.kingssack.volt.opmode.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.robot.GabePP
import org.firstinspires.ftc.teamcode.robot.drivetrain
import org.firstinspires.ftc.teamcode.robot.launcher
import org.firstinspires.ftc.teamcode.robot.storage
import org.firstinspires.ftc.teamcode.util.AllianceColor
import org.firstinspires.ftc.teamcode.util.PathConstants
import org.firstinspires.ftc.teamcode.util.toRadians

@Config
abstract class Finch(alliance: AllianceColor) :
    AutonomousMode<GabePP>({ hardwareMap ->
        GabePP(hardwareMap, Pose(INITIAL_X, INITIAL_Y, INITIAL_HEADING.toRadians()).mirror())
    }) {
    companion object {
        @JvmField var INITIAL_X: Double = 56.0
        @JvmField var INITIAL_Y: Double = 8.0
        @JvmField var INITIAL_HEADING: Double = 90.0
    }

    private val paths by lazy { PathConstants(robot.drivetrain.follower, alliance) }

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
                +InstantAction { sleep(1000) }
                +close()
                +InstantAction { sleep(1000) }
            }
        }
        launcher {
            +disable()
        }
    }
}

@Autonomous(name = "Finch Blue", group = "Competition", preselectTeleOp = "Manatee")
class FinchBlue : Finch(AllianceColor.BLUE)

@Autonomous(name = "Finch Blue", group = "Competition", preselectTeleOp = "Manatee")
class FinchRed : Finch(AllianceColor.RED)
