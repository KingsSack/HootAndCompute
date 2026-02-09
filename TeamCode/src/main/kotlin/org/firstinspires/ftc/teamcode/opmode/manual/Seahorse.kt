package org.firstinspires.ftc.teamcode.opmode.manual

import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.hardware.Gamepad
import dev.kingssack.volt.attachment.drivetrain.MecanumDriveWithPP
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.opmode.manual.SimpleManualModeWithSpeedModes
import dev.kingssack.volt.util.GamepadButton
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.attachment.Classifier
import org.firstinspires.ftc.teamcode.robot.Jones
import org.firstinspires.ftc.teamcode.robot.JonesPP
import dev.kingssack.volt.opmode.autonomous.AllianceColor

@Suppress("unused")
@VoltOpModeMeta("Seahorse", "Competition")
class Seahorse : SimpleManualModeWithSpeedModes<MecanumDriveWithPP, JonesPP>() {
    override val robot: JonesPP = JonesPP(hardwareMap)
    var targetVelocity = Jones.launcherTargetVelocity
    var allianceColor = blackboard["allianceColor"] as? AllianceColor ?: AllianceColor.BLUE

    init {
        // Launcher
        onButtonTapped(GamepadButton.RIGHT_BUMPER2) {
            with(robot) { +launcher.enable(targetVelocity) }
        }
        onButtonTapped(GamepadButton.LEFT_BUMPER2) { with(robot) { +launcher.disable() } }
        onButtonTapped(GamepadButton.DPAD_LEFT2) {
            with(robot) {
                targetVelocity = Jones.launcherLowVelocity
                if (launcher.currentVelocity > 0.0) +launcher.enable(targetVelocity)
            }
        }
        onButtonTapped(GamepadButton.DPAD_UP2) {
            with(robot) {
                targetVelocity = Jones.launcherMediumVelocity
                if (launcher.currentVelocity > 0.0) +launcher.enable(targetVelocity)
            }
        }
        onButtonTapped(GamepadButton.DPAD_RIGHT2) {
            with(robot) {
                targetVelocity = Jones.launcherTargetVelocity
                if (launcher.currentVelocity > 0.0) +launcher.enable(targetVelocity)
            }
        }

        // Classifier
        onButtonTapped(GamepadButton.DPAD_LEFT1) { with(robot) { +classifier.goToPos(1) } }
        onButtonTapped(GamepadButton.DPAD_UP1) { with(robot) { +classifier.goToPos(2) } }
        onButtonTapped(GamepadButton.DPAD_RIGHT1) { with(robot) { +classifier.goToPos(3) } }

        onButtonTapped(GamepadButton.A2) {
            with(robot) { +classifier.releaseArtifact(Classifier.ReleaseType.NEXT) }
        }
        onButtonTapped(GamepadButton.X2) {
            with(robot) { +classifier.releaseArtifact(Classifier.ReleaseType.PURPLE) }
        }
        onButtonTapped(GamepadButton.Y2) {
            with(robot) { +classifier.releaseArtifact(Classifier.ReleaseType.GREEN) }
        }

        // Pusher
        onButtonTapped(GamepadButton.B2) { with(robot) { +pusher.push() } }
        onButtonReleased(GamepadButton.B2) { with(robot) { +pusher.retract() } }

        // Aiming
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
        with(robot.drivetrain) {
            startTeleOpDrive()
            pose = blackboard["endPose"] as? Pose ?: pose
        }
    }

    context(telemetry: Telemetry)
    override fun tick() =
        with(telemetry) {
            addData("Alliance Color", allianceColor)
            addData("Target Velocity", targetVelocity)

            if (robot.launcher.isAtSpeed && robot.launcher.currentVelocity > 0.0) {
                gamepad2.setLedColor(0.0, 1.0, 0.0, Gamepad.LED_DURATION_CONTINUOUS)
                gamepad2.rumble(1.0, 1.0, Gamepad.RUMBLE_DURATION_CONTINUOUS)
            } else {
                gamepad2.setLedColor(1.0, 0.0, 0.0, Gamepad.LED_DURATION_CONTINUOUS)
                gamepad2.stopRumble()
            }

            super.tick()
        }
}
