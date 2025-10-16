package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.opmode.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.robot.Jones

@Config
@Autonomous(name = "Magpie", group = "Competition")
class Magpie : AutonomousMode<Jones>() {
    companion object {
        @JvmField var INITIAL_X: Double = 0.0
        @JvmField var INITIAL_Y: Double = 0.0
        @JvmField var INITIAL_HEADING: Double = 0.0
    }

    override fun createRobot(hardwareMap: HardwareMap): Jones {
        return Jones(
            hardwareMap,
            Pose2d(Vector2d(INITIAL_X, INITIAL_Y), Math.toRadians(INITIAL_HEADING))
        )
    }
}