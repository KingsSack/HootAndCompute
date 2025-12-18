package org.firstinspires.ftc.teamcode.opmode.manual

import dev.kingssack.volt.attachment.drivetrain.MecanumDriveWithPP
import dev.kingssack.volt.opmode.manual.SimpleManualModeWithSpeedModes
import org.firstinspires.ftc.teamcode.robot.GabePP

@Suppress("unused")
class Narwhal() :
    SimpleManualModeWithSpeedModes<MecanumDriveWithPP, GabePP>({ hardwareMap ->
        GabePP(hardwareMap)
    }) {
    override val name = "Narwhal"
    override fun initialize() {
        super.initialize()
        robot.drivetrain.startTeleOpDrive()
    }
}
