package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.roadrunner.*
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.robot.Jones

@Autonomous(name = "catonomousmode", group = "test")
class Catonomousmode : AutonomousMode<Jones>() {
    override fun createRobot(hardwareMap: HardwareMap): Jones {
        return Jones(
            hardwareMap,
            Pose2d(Vector2d(0.0, 0.0), Math.toRadians(90.0))
        )
    }
// *this is a orientation test mode
    init {
        actionSequence.add { moveupanddown() }
    }
    fun moveupanddown() : Action {
        return SequentialAction(
            robot.strafeTo(Vector2d(0.0, 6.0)),
            robot.strafeTo(Vector2d(0.0, -6.0))
        )
    }
}