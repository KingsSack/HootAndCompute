package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.roadrunner.Action
import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.SequentialAction
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.robot.Jones

@Autonomous(name = "rhino", group = "test")
class rhino : AutonomousMode<Jones>() {
    override fun createRobot(hardwareMap: HardwareMap): Jones {
        return Jones(
            hardwareMap,
            Pose2d(Vector2d(0.0, 0.0), heading = Math.toRadians(0.0))
        )
    }
    init {
        actionSequence.add { moveforward() }
    }
    fun moveforward() : Action {
        return SequentialAction(
            robot.strafeTo(Vector2d(3.0, 0.0))
        )
    }
}