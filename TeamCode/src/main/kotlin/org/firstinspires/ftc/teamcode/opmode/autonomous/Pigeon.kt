package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.pedropathing.geometry.Pose
import dev.kingssack.volt.opmode.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.robot.GabePP
import dev.kingssack.volt.opmode.autonomous.AllianceColor
import org.firstinspires.ftc.teamcode.util.PathConstants

@Suppress("unused")
class Pigeon() : AutonomousMode<GabePP>({ hardwareMap -> GabePP(hardwareMap, Pose(57.0, 9.0, 90.0)) }) {
    override val name = "Pigeon"
    override val group = "Competition"
    override val autoTransition = "Manatee"
    private val paths by lazy { PathConstants(robot.drivetrain.follower, AllianceColor.BLUE) }

    override fun sequence() = execute { +robot.drivetrain.pathTo(paths.pathOffWallLaunchLine) }
}
