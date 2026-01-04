package org.firstinspires.ftc.teamcode.opmode.manual

import dev.kingssack.volt.attachment.drivetrain.MecanumDriveWithPP
import dev.kingssack.volt.opmode.manual.SimpleManualModeWithSpeedModes
import dev.kingssack.volt.util.GamepadAnalogInput
import dev.kingssack.volt.util.GamepadButton
import org.firstinspires.ftc.teamcode.robot.JonesPP

@Suppress("unused")
class Seahorse :
    SimpleManualModeWithSpeedModes<MecanumDriveWithPP, JonesPP>({ hardwareMap ->
        JonesPP(hardwareMap)
    }) {
    override val name= "Seahorse"
    override val group = "Competition"
    init {
        // Launcher
        onButtonTapped(GamepadButton.RIGHT_BUMPER2) { +robot.launcher.enable() }
        onButtonReleased(GamepadButton.RIGHT_BUMPER2) { +robot.launcher.disable() }
        onAnalog(GamepadAnalogInput.RIGHT_TRIGGER2) { value ->
            if (!isButtonPressed(GamepadButton.RIGHT_BUMPER2)) {
                robot.launcher.setPower(value.toDouble())
            }
        }
    }
}
