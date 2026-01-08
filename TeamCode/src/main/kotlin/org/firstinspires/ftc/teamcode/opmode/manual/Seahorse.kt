package org.firstinspires.ftc.teamcode.opmode.manual

import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.attachment.drivetrain.MecanumDriveWithPP
import dev.kingssack.volt.opmode.manual.SimpleManualModeWithSpeedModes
import dev.kingssack.volt.util.GamepadButton
import org.firstinspires.ftc.teamcode.robot.JonesPP

@Suppress("unused")
class Seahorse : SimpleManualModeWithSpeedModes<MecanumDriveWithPP, JonesPP>() {
    override val name= "Seahorse"
    override val group = "Competition"
    override fun getRobot(hardwareMap: HardwareMap): JonesPP {
        return JonesPP(hardwareMap)
    }

    init {
        // Launcher
        onButtonTapped(GamepadButton.RIGHT_BUMPER2) { +robot.launcher.enable() }
        onButtonReleased(GamepadButton.RIGHT_BUMPER2) { +robot.launcher.disable() }
    }
}
