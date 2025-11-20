package org.firstinspires.ftc.teamcode.opmode.manual

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import dev.kingssack.volt.attachment.drivetrain.MecanumDriveWithPP
import dev.kingssack.volt.opmode.manual.SimpleManualModeWithSpeedModes
import org.firstinspires.ftc.teamcode.robot.JonesPP

@TeleOp(name = "Narwhal", group = "Default")
class Narwhal :
    SimpleManualModeWithSpeedModes<MecanumDriveWithPP, JonesPP>({ hardwareMap ->
        JonesPP(hardwareMap)
    }) {
    override fun initialize() {
        robot.drivetrain.startTeleOpDrive()
    }
}
