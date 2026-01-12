package org.firstinspires.ftc.teamcode.opmode.manual

import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.attachment.drivetrain.MecanumDriveWithPP
import dev.kingssack.volt.opmode.manual.SimpleManualModeWithSpeedModes
import dev.kingssack.volt.util.GamepadButton
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.robot.JonesPP
import org.firstinspires.ftc.teamcode.util.AllianceColor

@Suppress("unused")
class Seahorse : SimpleManualModeWithSpeedModes<MecanumDriveWithPP, JonesPP>() {

    override val name= "Seahorse"
    override val group = "Competition"
    override fun getRobot(hardwareMap: HardwareMap): JonesPP {
        return JonesPP(hardwareMap)
    }
    var allianceColor = AllianceColor.BLUE
    init {
        // Launcher
        onButtonTapped(GamepadButton.RIGHT_BUMPER2) { +robot.launcher.enable() }
        onButtonReleased(GamepadButton.RIGHT_BUMPER2) { +robot.launcher.disable() }
        onButtonReleased(GamepadButton.DPAD_UP2) {
            +robot.launcher.increaseVelocity(100.0)
            gamepad2.rumble(0.5, 0.5, 200)
        }
        onButtonReleased(GamepadButton.DPAD_DOWN2) {
            +robot.launcher.decreaseVelocity(100.0)
            gamepad2.rumble(0.5, 0.5, 200)
        }

        onButtonDoubleTapped(GamepadButton.LEFT_BUMPER1) {
            allianceColor =
                if (allianceColor == AllianceColor.RED) AllianceColor.BLUE else AllianceColor.RED
            gamepad1.rumble(0.5, 0.5, 300)
        }
        context(telemetry) {
            onButtonTapped(GamepadButton.RIGHT_BUMPER1) {
                +robot.pointTowardsAprilTag(allianceColor)
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
        super.tick()
    }
}
