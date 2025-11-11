package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.dashboard.config.Config
import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import dev.kingssack.volt.opmode.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.robot.Jones
import org.firstinspires.ftc.teamcode.util.toRadians

@Config
@Autonomous(name = "Magpie", group = "Competition")
class Magpie :
    AutonomousMode<Jones>(
        robotFactory = { hardwareMap ->
            Jones(hardwareMap, Pose(INITIAL_X, INITIAL_Y, INITIAL_HEADING.toRadians()))
        }
    ) {
    companion object {
        @JvmField var INITIAL_X: Double = 0.0
        @JvmField var INITIAL_Y: Double = 0.0
        @JvmField var INITIAL_HEADING: Double = 0.0
    }

    override fun sequence() = execute {}
}
