package org.firstinspires.ftc.teamcode.opmode.manual

import com.acmerobotics.roadrunner.InstantAction
import com.pedropathing.geometry.Pose
import com.qualcomm.hardware.rev.RevBlinkinLedDriver
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.Gamepad
import dev.kingssack.volt.attachment.drivetrain.MecanumDriveWithPP
import dev.kingssack.volt.opmode.manual.SimpleManualModeWithSpeedModes
import dev.kingssack.volt.util.GamepadButton
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.robot.GabePP
import org.firstinspires.ftc.teamcode.util.AllianceColor

@TeleOp(name = "Manatee", group = "Competition")
class Manatee : SimpleManualModeWithSpeedModes<MecanumDriveWithPP, GabePP>({ GabePP(it) }) {
    var targetVelocity = 1500.0
    var modifyScale = 100.0

    init {
        // Launcher
        onButtonTapped(GamepadButton.RIGHT_BUMPER2) {
            with(robot) { +launcher.enable(targetVelocity) }
        }
        onButtonReleased(GamepadButton.RIGHT_BUMPER2) {
            with(robot) {
                +launcher.disable()
                gamepad2.stopRumble()
                setRGBPatternToAllianceColor(allianceColor)
            }
        }
        whileButtonHeld(GamepadButton.RIGHT_BUMPER1) {
            if (launcher.isAtSpeed) rgb.setPattern(RevBlinkinLedDriver.BlinkinPattern.CONFETTI)
            else setRGBPatternToAllianceColor(allianceColor)
        }
        onButtonReleased(GamepadButton.DPAD_UP2) {
            +InstantAction { targetVelocity += modifyScale }
            if (!gamepad2.isRumbling) gamepad2.rumble(0.5, 0.5, 100)
        }
        onButtonReleased(GamepadButton.DPAD_DOWN2) {
            +InstantAction { targetVelocity -= modifyScale }
            if (!gamepad2.isRumbling) gamepad2.rumble(0.5, 0.5, 100)
        }
        onButtonReleased(GamepadButton.DPAD_RIGHT2) { +InstantAction { modifyScale *= 10 } }
        onButtonReleased(GamepadButton.DPAD_LEFT2) { +InstantAction { modifyScale /= 10 } }

        // Storage
        onButtonTapped(GamepadButton.A2) { with(robot) { +storage.release() } }
        onButtonReleased(GamepadButton.A2) { with(robot) { +storage.close() } }

        // Aiming
        onButtonDoubleTapped(GamepadButton.LEFT_BUMPER1) {
            with(robot) {
                allianceColor =
                    if (allianceColor == AllianceColor.RED) AllianceColor.BLUE
                    else AllianceColor.RED
                if (!gamepad1.isRumbling) gamepad1.rumbleBlips(3)
            }
        }
        context(telemetry) {
            onButtonTapped(GamepadButton.RIGHT_BUMPER1) { with(robot) { +pointTowardsAprilTag() } }
        }
    }

    override fun initialize() {
        super.initialize()
        with(robot) {
            drivetrain.startTeleOpDrive()
            allianceColor = blackboard["allianceColor"] as? AllianceColor ?: allianceColor
            drivetrain.pose = blackboard["endPose"] as? Pose ?: drivetrain.pose
        }
    }

    context(telemetry: Telemetry)
    override fun tick() =
        with(telemetry) {
            addData("Target Velocity", targetVelocity)
            addData("Modify Scale", modifyScale)

            if (robot.launcher.isAtSpeed && robot.launcher.currentVelocity > 0.0) {
                gamepad2.setLedColor(0.0, 1.0, 0.0, Gamepad.LED_DURATION_CONTINUOUS)
                gamepad2.rumble(1.0, 1.0, Gamepad.RUMBLE_DURATION_CONTINUOUS)
            } else {
                gamepad2.setLedColor(1.0, 0.0, 0.0, Gamepad.LED_DURATION_CONTINUOUS)
            }

            super.tick()
        }
}
