package org.firstinspires.ftc.teamcode.opmode.manual

import com.qualcomm.robotcore.hardware.Gamepad
import dev.kingssack.volt.attachment.drivetrain.MecanumDriveWithPP
import dev.kingssack.volt.opmode.manual.SimpleManualModeWithSpeedModes
import dev.kingssack.volt.util.GamepadButton
import org.firstinspires.ftc.teamcode.robot.GabePP

@Suppress("unused")
class Manatee :
    SimpleManualModeWithSpeedModes<MecanumDriveWithPP, GabePP>({ hardwareMap ->
        GabePP(hardwareMap)
    }) {
    override val name =  "Manatee"
    override val group = "Competition"
    init {
        // Launcher
        onButtonTapped(GamepadButton.RIGHT_BUMPER2) { +robot.launcher.enable() }
        onButtonReleased(GamepadButton.RIGHT_BUMPER2) { +robot.launcher.disable() }
        whileButtonHeld(GamepadButton.RIGHT_BUMPER2) {
            gamepad2.rumble(1.0, 1.0, Gamepad.RUMBLE_DURATION_CONTINUOUS)
        }
        onButtonReleased(GamepadButton.DPAD_UP2) {
            +robot.launcher.increaseVelocity(100.0)
            gamepad2.rumble(0.5, 0.5, 200)
        }
        onButtonReleased(GamepadButton.DPAD_DOWN2) {
            +robot.launcher.decreaseVelocity(100.0)
            gamepad2.rumble(0.5, 0.5, 200)
        }

        // Storage
        onButtonTapped(GamepadButton.A2) { +robot.storage.release() }
        onButtonReleased(GamepadButton.A2) { +robot.storage.close() }
    }

    override fun initialize() {
        super.initialize()
        robot.drivetrain.startTeleOpDrive()
    }
}
