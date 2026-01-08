package org.firstinspires.ftc.teamcode.opmode.manual

import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.attachment.drivetrain.MecanumDriveWithPP
import dev.kingssack.volt.opmode.manual.SimpleManualModeWithSpeedModes
import org.firstinspires.ftc.teamcode.robot.GabePP

@Suppress("unused")
class Narwhal : SimpleManualModeWithSpeedModes<MecanumDriveWithPP, GabePP>() {
    override val name = "Narwhal"
    override fun getRobot(hardwareMap: HardwareMap): GabePP {
        return GabePP(hardwareMap)
    }

    override fun initialize() {
        super.initialize()
        robot.drivetrain.startTeleOpDrive()
    }
}
