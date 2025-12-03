package org.firstinspires.ftc.teamcode.opmode.manual

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import dev.kingssack.volt.attachment.drivetrain.MecanumDriveWithRR
import dev.kingssack.volt.opmode.manual.SimpleManualModeWithSpeedModes
import dev.kingssack.volt.util.GamepadAnalogInput
import dev.kingssack.volt.util.GamepadButton
import org.firstinspires.ftc.teamcode.robot.GabeRR

@TeleOp(name = "Manatee", group = "Competition")
class Manatee :
    SimpleManualModeWithSpeedModes<MecanumDriveWithRR, GabeRR>({ hardwareMap ->
        GabeRR(hardwareMap)
    }) {
    init {
        // Launcher
        onButtonTapped(GamepadButton.RIGHT_BUMPER2) { +robot.launcher.enable() }
        onButtonReleased(GamepadButton.RIGHT_BUMPER2) { +robot.launcher.disable() }
        onAnalog(GamepadAnalogInput.RIGHT_TRIGGER2) { value ->
            if (!isButtonPressed(GamepadButton.RIGHT_BUMPER2)) {
                robot.launcher.setPower(value.toDouble())
            }
        }

        // Storage
        onButtonTapped(GamepadButton.A2) { +robot.storage.release() }
        onButtonReleased(GamepadButton.A2) { +robot.storage.close() }
    }
}
