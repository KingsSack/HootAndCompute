package org.firstinspires.ftc.teamcode.opmode.manual

import com.qualcomm.robotcore.hardware.Gamepad
import dev.kingssack.volt.attachment.drivetrain.MecanumDriveWithRR
import dev.kingssack.volt.opmode.manual.SimpleManualModeWithSpeedModes
import dev.kingssack.volt.util.GamepadAnalogInput
import dev.kingssack.volt.util.GamepadButton
import org.firstinspires.ftc.teamcode.robot.GabeRR

@Suppress("unused")
class Manatee :
    SimpleManualModeWithSpeedModes<MecanumDriveWithRR, GabeRR>({ hardwareMap ->
        GabeRR(hardwareMap)
    }) {
    override val name =  "Manatee"
    override val group = "Competition"
    init {
        // Launcher
        onButtonTapped(GamepadButton.RIGHT_BUMPER2) { +robot.launcher.enable() }
        onButtonReleased(GamepadButton.RIGHT_BUMPER2) { +robot.launcher.disable() }
        whileButtonHeld(GamepadButton.RIGHT_BUMPER2) {
            gamepad2.rumble(0.0, 1.0, Gamepad.RUMBLE_DURATION_CONTINUOUS)
        }
        onAnalog(GamepadAnalogInput.RIGHT_TRIGGER2) { value ->
            if (!isButtonPressed(GamepadButton.RIGHT_BUMPER2)) {
                robot.launcher.setPower(value.toDouble())

                if (value > 0.0) {
                    gamepad2.rumble(0.0, value.toDouble(), Gamepad.RUMBLE_DURATION_CONTINUOUS)
                } else if (gamepad2.isRumbling) {
                    gamepad2.stopRumble()
                }
            }
        }

        // Storage
        onButtonTapped(GamepadButton.A2) { +robot.storage.release() }
        onButtonReleased(GamepadButton.A2) { +robot.storage.close() }
    }
}
