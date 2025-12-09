package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import dev.kingssack.volt.opmode.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.robot.GabeRR
import org.firstinspires.ftc.teamcode.robot.launcher
import org.firstinspires.ftc.teamcode.robot.storage
import org.firstinspires.ftc.teamcode.util.toRadians

@Autonomous(name = "Pigeon", group = "Competition", preselectTeleOp = "Manatee")
class Pigeon :
    AutonomousMode<GabeRR>({ hardwareMap ->
        GabeRR(hardwareMap, Pose2d(Vector2d(0.0, 0.0), 0.0))
    }) {
    override fun sequence() = execute {
        +robot.drivetrain.trajectory {
            strafeTo(Vector2d(0.0, 0.0))
            strafeToLinearHeading(Vector2d(48.0, 0.0), 0.0.toRadians())
        }

        sequence {
            launcher { enable() }
            storage { release() }
            launcher { disable() }
        }
    }
}
