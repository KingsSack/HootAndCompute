package org.firstinspires.ftc.teamcode.opmode.manual

import dev.kingssack.volt.attachment.drivetrain.MecanumDriveWithPP
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.opmode.manual.SimpleManualModeWithSpeedModes
import org.firstinspires.ftc.teamcode.robot.GabePP

@Suppress("unused")
@VoltOpModeMeta("Narwhal")
class Narwhal : SimpleManualModeWithSpeedModes<MecanumDriveWithPP, GabePP>() {
    override val robot: GabePP = GabePP(hardwareMap)

    override fun initialize() {
        super.initialize()
        robot.drivetrain.startTeleOpDrive()
    }
}
