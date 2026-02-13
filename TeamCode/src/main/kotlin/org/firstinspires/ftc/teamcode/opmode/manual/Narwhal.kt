package org.firstinspires.ftc.teamcode.opmode.manual

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import dev.kingssack.volt.attachment.drivetrain.MecanumDriveWithPP
import dev.kingssack.volt.opmode.manual.SimpleManualModeWithSpeedModes
import dev.kingssack.volt.util.buttons.ControlScope
import org.firstinspires.ftc.teamcode.robot.Gabe
import org.firstinspires.ftc.teamcode.robot.GabePP

@TeleOp(name = "Narwhal", group = "Default")
class Narwhal : SimpleManualModeWithSpeedModes<MecanumDriveWithPP, Gabe<MecanumDriveWithPP>>({ GabePP(it) }) {
    override val extraControls : ControlScope<Gabe<MecanumDriveWithPP>>.() -> Unit = {
        // No extra controls for now
    }

    override fun initialize() {
        super.initialize()
        robot.drivetrain.startTeleOpDrive()
    }
}
