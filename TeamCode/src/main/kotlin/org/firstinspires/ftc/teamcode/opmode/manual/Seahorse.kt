package org.firstinspires.ftc.teamcode.opmode.manual

import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.hardware.Gamepad
import dev.kingssack.volt.attachment.drivetrain.MecanumDriveWithPP
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.opmode.manual.SimpleManualModeWithSpeedModes
import dev.kingssack.volt.util.GamepadAnalogInput
import dev.kingssack.volt.util.GamepadButton
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.attachment.Classifier
import org.firstinspires.ftc.teamcode.robot.Jones
import org.firstinspires.ftc.teamcode.robot.JonesPP
import dev.kingssack.volt.opmode.autonomous.AllianceColor

@Suppress("unused")
@VoltOpModeMeta("Seahorse", "Competition")
class Seahorse :
    SimpleManualModeWithSpeedModes<MecanumDriveWithPP, JonesPP>() {
    override val robot: JonesPP = JonesPP(hardwareMap, blackboard["endPose"] as? Pose ?: Pose())
    var targetVelocity = Jones.launcherTargetVelocity
    var allianceColor = blackboard["allianceColor"] as? AllianceColor ?: AllianceColor.BLUE

    var position: Int = 0

    init {
        // Launcher
        onButtonTapped(GamepadButton.RIGHT_BUMPER2) {
            with(robot) { +launcher.enable(targetVelocity) }
        }
        onButtonTapped(GamepadButton.LEFT_BUMPER2) { with(robot) { +launcher.disable() } }
        onButtonTapped(GamepadButton.DPAD_LEFT2) {
            with(robot) {
                instant { targetVelocity = Jones.launcherLowVelocity }
                if (launcher.currentVelocity > 0.0) +launcher.enable(targetVelocity)
            }
        }
        onButtonTapped(GamepadButton.DPAD_UP2) {
            with(robot) {
                instant { targetVelocity = Jones.launcherMediumVelocity }
                if (launcher.currentVelocity > 0.0) +launcher.enable(targetVelocity)
            }
        }
        onButtonTapped(GamepadButton.DPAD_RIGHT2) {
            with(robot) {
                instant { targetVelocity = Jones.launcherTargetVelocity }
                if (launcher.currentVelocity > 0.0) +launcher.enable(targetVelocity)
            }
        }

        // Classifier
        onButtonTapped(GamepadButton.A2) {
            with(robot) { +classifier.releaseArtifact(Classifier.ReleaseType.NEXT) }
        }
        onButtonTapped(GamepadButton.X2) {
            with(robot) { +classifier.releaseArtifact(Classifier.ReleaseType.PURPLE) }
        }
        onButtonTapped(GamepadButton.Y2) {
            with(robot) { +classifier.releaseArtifact(Classifier.ReleaseType.GREEN) }
        }
        onButtonTapped(GamepadButton.DPAD_DOWN1) {
            with(robot) {
                instant { position++ }
                +classifier.goToPos(position % 3 + 1)
            }
        }

        // Pusher
        onButtonTapped(GamepadButton.B2) { with(robot) { +pusher.push() } }
        onButtonReleased(GamepadButton.B2) { with(robot) { +pusher.retract() } }

        // Aiming
        context(telemetry) {
            onAnalog(GamepadAnalogInput.RIGHT_TRIGGER1) { value ->
                with(robot) {
                    if (value <= 0.3) {
                        aprilTagAiming.reset()
                        return@onAnalog
                    }

                    val targetId = if (allianceColor == AllianceColor.BLUE) 20 else 24
                    val tag = getDetectedAprilTags(targetId).firstOrNull()

                    rx =
                        if (tag != null) {
                            aprilTagAiming.pointTowardsAprilTag(tag)
                        } else {
                            aprilTagAiming.reset()
                            0.0
                        }
                }
            }
        }
        // Automatic firing
        onButtonTapped(GamepadButton.DPAD_DOWN2) {
            with(robot) { +fireAllStoredArtifacts(targetVelocity) }
        }
    }

    init {
        with(robot) { drivetrain.startTeleOpDrive() }
    }

    context(telemetry: Telemetry)
    override fun tick() =
        with(telemetry) {
            addData("Alliance Color", allianceColor)
            addData("Target Velocity", targetVelocity)
            addData("Classifier Position", position % 3 + 1)

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
