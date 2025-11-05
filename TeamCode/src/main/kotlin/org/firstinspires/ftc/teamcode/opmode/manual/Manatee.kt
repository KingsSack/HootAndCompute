package org.firstinspires.ftc.teamcode.opmode.manual

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import dev.kingssack.volt.opmode.manual.SimpleManualModeWithSpeedModes
import org.firstinspires.ftc.teamcode.robot.Jones

@Config
@TeleOp(name = "Manatee", group = "Default")
class Manatee :
    SimpleManualModeWithSpeedModes<Jones>(
        robotFactory = { hardwareMap ->
            Jones(
                hardwareMap,
                Pose2d(Vector2d(INITIAL_X, INITIAL_Y), Math.toRadians(INITIAL_HEADING)),
            )
        }
    ) {
    companion object {
        @JvmField var INITIAL_X: Double = 0.0
        @JvmField var INITIAL_Y: Double = 0.0
        @JvmField var INITIAL_HEADING: Double = 0.0
    }

    override fun tick() {
        robot.setDrivePowers(calculatePoseWithGamepad())
        super.tick()
    }
}