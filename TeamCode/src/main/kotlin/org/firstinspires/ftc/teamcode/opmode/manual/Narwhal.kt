package org.firstinspires.ftc.teamcode.opmode.manual

import dev.kingssack.volt.attachment.drivetrain.pp.mecanum.DriveEncoderMecanumPedroPathingDrivetrain
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.opmode.manual.DrivetrainControlsManualMode
import org.firstinspires.ftc.teamcode.robot.GabePP

@VoltOpModeMeta("Narwhal")
class Narwhal : DrivetrainControlsManualMode<DriveEncoderMecanumPedroPathingDrivetrain, GabePP>() {
    override val robot = GabePP(hardwareMap)

    init {
        robot.drivetrain.startTeleOpDrive()
    }
}
