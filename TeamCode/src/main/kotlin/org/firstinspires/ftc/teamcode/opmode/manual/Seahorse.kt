package org.firstinspires.ftc.teamcode.opmode.manual

import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.Gamepad
import dev.kingssack.volt.attachment.drivetrain.MecanumDriveWithPP
import dev.kingssack.volt.opmode.manual.SimpleManualModeWithSpeedModes
import dev.kingssack.volt.util.buttons.ControlScope
import dev.kingssack.volt.util.buttons.GamepadAnalogInput
import dev.kingssack.volt.util.buttons.GamepadButton
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.attachment.Classifier
import org.firstinspires.ftc.teamcode.attachment.Launcher
import org.firstinspires.ftc.teamcode.attachment.Pusher
import org.firstinspires.ftc.teamcode.robot.Jones
import org.firstinspires.ftc.teamcode.robot.JonesPP
import org.firstinspires.ftc.teamcode.util.AllianceColor

@TeleOp(name = "Seahorse", group = "Competition")
class Seahorse :
    SimpleManualModeWithSpeedModes<MecanumDriveWithPP, Jones<MecanumDriveWithPP>>({
        JonesPP(it, blackboard["endPose"] as? Pose ?: Pose())
    }) {
    // --- State ---

    var targetVelocity = Jones.launcherTargetVelocity

    val allianceColor: AllianceColor by lazy {
        blackboard["allianceColor"] as? AllianceColor ?: AllianceColor.BLUE
    }

    var position: Int = 0

    // --- Controls ---

    private val Launcher.controls
        get() = controls {
            GamepadButton.RIGHT_BUMPER2.onTap { +enable(targetVelocity) }
            GamepadButton.LEFT_BUMPER2.onTap { +disable() }
            GamepadButton.DPAD_LEFT2.onTap {
                instant { targetVelocity = Jones.launcherLowVelocity }
                if (currentVelocity > 0.0) +enable(targetVelocity)
            }
            GamepadButton.DPAD_UP2.onTap {
                instant { targetVelocity = Jones.launcherMediumVelocity }
                if (currentVelocity > 0.0) +enable(targetVelocity)
            }
            GamepadButton.DPAD_RIGHT2.onTap {
                instant { targetVelocity = Jones.launcherTargetVelocity }
                if (currentVelocity > 0.0) +enable(targetVelocity)
            }
        }

    private val Classifier.controls
        get() = controls {
            GamepadButton.A2.onTap { +releaseArtifact(Classifier.ReleaseType.NEXT) }
            GamepadButton.X2.onTap { +releaseArtifact(Classifier.ReleaseType.PURPLE) }
            GamepadButton.Y2.onTap { +releaseArtifact(Classifier.ReleaseType.GREEN) }
            GamepadButton.DPAD_DOWN1.onTap {
                instant { position++ }
                +goToPos(position % 3 + 1)
            }
        }

    private val Pusher.controls
        get() = controls {
            GamepadButton.B2.onTap { +push() }
            GamepadButton.B2.onRelease { +retract() }
        }

    private val aimingControls = controls {
        GamepadAnalogInput.RIGHT_TRIGGER1.whenAbove { value ->
            context(telemetry) {
                if (value <= 0.3) {
                    aprilTagAiming.reset()
                    return@whenAbove
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

    private val autoFireControls = controls {
        GamepadButton.DPAD_DOWN2.onTap { +robot.fireAllStoredArtifacts(targetVelocity) }
    }

    override val extraControls: ControlScope<Jones<MecanumDriveWithPP>>.() -> Unit = {
        robot.launcher.controls
        robot.classifier.controls
        robot.pusher.controls
        aimingControls
        autoFireControls
    }

    override fun initialize() {
        super.initialize()
        robot.drivetrain.startTeleOpDrive()
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
