package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.dashboard.config.Config
import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import dev.kingssack.volt.opmode.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.robot.JonesPP
import org.firstinspires.ftc.teamcode.util.AllianceColor
import org.firstinspires.ftc.teamcode.util.PathConstants
import org.firstinspires.ftc.teamcode.util.toRadians

@Config
@Autonomous(name = "Magpie", group = "Competition", preselectTeleOp = "Seahorse")
class Magpie :
    AutonomousMode<JonesPP>({ hardwareMap ->
        JonesPP(hardwareMap, Pose(INITIAL_X, INITIAL_Y, INITIAL_HEADING.toRadians()).mirror())
    }) {
    companion object {
        @JvmField var INITIAL_X: Double = 56.0
        @JvmField var INITIAL_Y: Double = 8.0
        @JvmField var INITIAL_HEADING: Double = 90.0
    }

    private val paths by lazy { PathConstants(robot.drivetrain.follower, AllianceColor.BLUE) }

    override fun sequence() = execute {
        with(robot) {
            val tags = context(telemetry) { getDetectedAprilTags() }
            +drivetrain.pathTo(paths.pathToLaunchZoneFromWall)
            when (tags[0].id) {
                21 -> telemetry.addData("Pattern", "GPP")
                22 -> telemetry.addData("Pattern", "PGP")
                23 -> telemetry.addData("Pattern", "PPG")
            }
        }
    }
}
