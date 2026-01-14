package org.firstinspires.ftc.teamcode.opmode.manual

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import dev.kingssack.volt.attachment.drivetrain.MecanumDriveWithPP
import dev.kingssack.volt.opmode.manual.SimpleManualModeWithSpeedModes
import org.firstinspires.ftc.teamcode.robot.GabePP

@TeleOp(name = "Narwhal", group = "Default")
class Narwhal : SimpleManualModeWithSpeedModes<MecanumDriveWithPP, GabePP>({ GabePP(it) }) {
    override fun initialize() {
        super.initialize()
        robot.drivetrain.startTeleOpDrive()
    }
}
