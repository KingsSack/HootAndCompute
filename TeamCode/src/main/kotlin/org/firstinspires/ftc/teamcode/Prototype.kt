package org.firstinspires.ftc.teamcode

import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import dev.kingssack.volt.opmode.manual.ManualMode
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.util.buttons.GamepadButton
import org.firstinspires.ftc.teamcode.rr.TestRobot

@TeleOp(name = "Prototype", group = "Prototype")
class Prototype :
    ManualMode<Robot>(
        robotFactory = { hardwareMap -> TestRobot(hardwareMap, Pose2d(Vector2d(0.0, 0.0), 0.0)) }
    ) {
    override val controls = controls {
        GamepadButton.A1.onRelease { instant { telemetry.addData("Button", "A1 released") } }
    }
}
