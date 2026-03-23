package org.firstinspires.ftc.teamcode.opmode.manual

import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.Vector2d
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.opmode.manual.ManualMode
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.util.Event.ManualEvent.Release
import dev.kingssack.volt.util.buttons.Button
import org.firstinspires.ftc.teamcode.rr.TestRobot

@VoltOpModeMeta("Prototype", "Prototype")
class Prototype : ManualMode<Robot>() {
    override val robot: Robot = TestRobot(hardwareMap, Pose2d(Vector2d(0.0, 0.0), 0.0))

    init {
        Release(Button.A1) then { instant { telemetry.addData("Button", "A1 released") } }
    }
}
