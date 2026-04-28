package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.pedropathing.geometry.Pose
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.opmode.autonomous.AutonomousMode
import dev.kingssack.volt.util.Event.AutonomousEvent.Start
import org.firstinspires.ftc.teamcode.robot.JonesPP
import org.firstinspires.ftc.teamcode.util.toRadians

@VoltOpModeMeta("Ivory", "Showcase")
class Ivory : AutonomousMode<JonesPP>() {
    val initialPose = Pose(72.0, 8.0, 90.0.toRadians())

    override val robot = JonesPP(hardwareMap, initialPose)

    init {
        // Navigates around two points
        Start then {
            +robot.drivetrain.path {
                splineToConstantHeading(
                    initialPose,
                    Pose(54.0, 16.0),
                    Pose(54.0, 54.0),
                    Pose(72.0, 60.0),
                )

                splineToConstantHeading(
                    Pose(72.0, 60.0),
                    Pose(90.0, 66.0),
                    Pose(90.0, 104.0),
                    Pose(72.0, 108.0),
                )
            }
        }
    }
}
