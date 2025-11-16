package org.firstinspires.ftc.teamcode.opmode.manual

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import dev.kingssack.volt.opmode.manual.SimpleManualModeWithSpeedModes
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.robot.JonesPP

@TeleOp(name = "Narwhal", group = "Default")
class Narwhal : SimpleManualModeWithSpeedModes<JonesPP>({ hardwareMap -> JonesPP(hardwareMap) }) {
    init {
        // Add any button bindings or initialization code here if needed
    }

    override fun initialize() {
        robot.drivetrain.startTeleOpDrive()
    }

    context(telemetry: Telemetry)
    override fun tick() {
        robot.drivetrain.setDrivePowers(calculatePoseWithGamepad())
        super.tick()
    }
}
