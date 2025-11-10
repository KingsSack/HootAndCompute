package org.firstinspires.ftc.teamcode.opmode.manual

import com.acmerobotics.dashboard.config.Config
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import dev.kingssack.volt.opmode.manual.SimpleManualModeWithSpeedModes
import dev.kingssack.volt.util.GamepadButton
import org.firstinspires.ftc.teamcode.robot.Jones

@Config
@TeleOp(name = "Manatee", group = "Default")
class Manatee :
    SimpleManualModeWithSpeedModes<Jones>(robotFactory = { hardwareMap -> Jones(hardwareMap) }) {
    init {
        onButtonPressed(GamepadButton.RIGHT_BUMPER2) { +{ robot.launcher.enable() } }
        onButtonTapped(GamepadButton.RIGHT_BUMPER2) { +{ robot.launcher.disable() } }
    }

    override fun initialize() {
        robot.drivetrain.startTeleOpDrive()
    }

    override fun tick() {
        robot.drivetrain.setDrivePowers(calculatePoseWithGamepad())
        super.tick()
    }
}
