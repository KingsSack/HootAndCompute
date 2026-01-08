package org.firstinspires.ftc.teamcode.opmode.manual

import com.acmerobotics.roadrunner.InstantAction
import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.opmode.manual.ManualMode
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.util.GamepadButton
import org.firstinspires.ftc.teamcode.rr.TestRobot

class Prototype : ManualMode<Robot>() {
    override val name = "Prototype"
    override val group = "Prototype"
    override fun getRobot(hardwareMap: HardwareMap): Robot {
        return TestRobot(hardwareMap, Pose2d(Vector2d(0.0, 0.0), 0.0))
    }

    init {
        onButtonReleased(GamepadButton.A1) {
            +InstantAction { telemetry.addData("Action", "Triggered") }
        }
    }
}