package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import dev.kingssack.volt.opmode.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.robot.GabePP
import org.firstinspires.ftc.teamcode.util.AllianceColor
import org.firstinspires.ftc.teamcode.util.PathConstants

@Autonomous(name = "Pigeon", group = "Competition", preselectTeleOp = "Manatee")
class Pigeon : AutonomousMode<GabePP>({ hardwareMap -> GabePP(hardwareMap, Pose(57.0, 9.0, 90.0)) }) {
    private val paths by lazy { PathConstants(robot.drivetrain.follower, AllianceColor.BLUE) }

    override fun sequence() = execute { +robot.drivetrain.pathTo(paths.pathOffWallLaunchLine) }
}
