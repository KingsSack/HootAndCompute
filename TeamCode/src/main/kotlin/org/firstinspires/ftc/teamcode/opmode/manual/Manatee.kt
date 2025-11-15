package org.firstinspires.ftc.teamcode.opmode.manual

import com.acmerobotics.dashboard.config.Config
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import dev.kingssack.volt.opmode.manual.SimpleManualModeWithSpeedModes
import dev.kingssack.volt.util.GamepadButton
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.robot.Gabe

@Config
@TeleOp(name = "Manatee", group = "Competition")
class Manatee : SimpleManualModeWithSpeedModes<Gabe>({ hardwareMap -> Gabe(hardwareMap) }) {
    init {
        onButtonPressed(GamepadButton.RIGHT_BUMPER2) { +robot.launcher.enable() }
        onButtonReleased(GamepadButton.RIGHT_BUMPER2) { +robot.launcher.disable() }

        onButtonPressed(GamepadButton.A2) { +robot.storage.release() }
        onButtonReleased(GamepadButton.A2) { +robot.storage.close() }
    }

    context(telemetry: Telemetry)
    override fun tick() {
        robot.drivetrain.setDrivePowers(calculatePoseWithGamepad())
        super.tick()
    }
}
