package org.firstinspires.ftc.teamcode.opmode.manual

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.opmode.manual.SimpleManualModeWithSpeedModes
import org.firstinspires.ftc.teamcode.robot.Jones

@Config
@TeleOp(name = "Manatee", group = "Default")
class Manatee : SimpleManualModeWithSpeedModes<Jones>() {
    /**
     * ManateeParams is a configuration object for manual control.
     *
     * @property INITIAL_X the initial x position
     * @property INITIAL_Y the initial y position
     * @property INITIAL_HEADING the initial heading
     */
    companion object ManateeParams {
        @JvmField var INITIAL_X: Double = 0.0
        @JvmField var INITIAL_Y: Double = 0.0
        @JvmField var INITIAL_HEADING: Double = 0.0
    }

    override fun createRobot(hardwareMap: HardwareMap): Jones {
        return Jones(
            hardwareMap,
            Pose2d(Vector2d(INITIAL_X, INITIAL_Y), Math.toRadians(INITIAL_HEADING)),
        )
    }

    init {
        interactions.addAll(listOf())
    }

    override fun tick() {
        robot.setDrivePowers(calculatePoseWithGamepad())
        super.tick()
    }
}