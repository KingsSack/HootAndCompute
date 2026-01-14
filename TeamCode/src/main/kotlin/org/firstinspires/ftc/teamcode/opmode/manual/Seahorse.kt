package org.firstinspires.ftc.teamcode.opmode.manual

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.Gamepad
import dev.kingssack.volt.attachment.drivetrain.MecanumDriveWithPP
import dev.kingssack.volt.opmode.manual.SimpleManualModeWithSpeedModes
import dev.kingssack.volt.util.GamepadButton
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.robot.JonesPP
import org.firstinspires.ftc.teamcode.util.AllianceColor

@TeleOp(name = "Seahorse", group = "Competition")
class Seahorse : SimpleManualModeWithSpeedModes<MecanumDriveWithPP, JonesPP>({ JonesPP(it) }) {
    var allianceColor = AllianceColor.BLUE

    init {
        // Launcher
        onButtonTapped(GamepadButton.RIGHT_BUMPER2) { with(robot) { +launcher.enable() } }
        onButtonReleased(GamepadButton.RIGHT_BUMPER2) { with(robot) { +launcher.disable() } }
        onButtonReleased(GamepadButton.DPAD_UP2) {
            with(robot) {
                +launcher.changeVelocity(100.0)
                gamepad2.rumble(0.5, 0.5, 100)
            }
        }
        onButtonReleased(GamepadButton.DPAD_DOWN2) {
            with(robot) {
                +launcher.changeVelocity(-100.0)
                gamepad2.rumble(0.5, 0.5, 100)
            }
        }

        onButtonDoubleTapped(GamepadButton.LEFT_BUMPER1) {
            allianceColor =
                if (allianceColor == AllianceColor.RED) AllianceColor.BLUE else AllianceColor.RED
            gamepad1.rumble(0.5, 0.5, 300)
        }
        context(telemetry) {
            onButtonTapped(GamepadButton.RIGHT_BUMPER1) {
                with(robot) { +pointTowardsAprilTag(allianceColor) }
            }
        }
    }

    override fun initialize() {
        super.initialize()
        robot.drivetrain.startTeleOpDrive()
    }

    context(telemetry: Telemetry)
    override fun tick() {
        telemetry.addData("Alliance Color", allianceColor)

        if (robot.launcher.isAtSpeed && robot.launcher.currentVelocity > 0.0) {
            gamepad2.setLedColor(0.0, 1.0, 0.0, Gamepad.LED_DURATION_CONTINUOUS)
            gamepad2.rumble(1.0, 1.0, Gamepad.RUMBLE_DURATION_CONTINUOUS)
        } else {
            gamepad2.setLedColor(1.0, 0.0, 0.0, Gamepad.LED_DURATION_CONTINUOUS)
        }

        super.tick()
    }
}
