package org.firstinspires.ftc.teamcode.opmode.manual

import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.Gamepad
import dev.kingssack.volt.attachment.drivetrain.MecanumDriveWithPP
import dev.kingssack.volt.opmode.manual.SimpleManualModeWithSpeedModes
import dev.kingssack.volt.util.buttons.ControlScope
import dev.kingssack.volt.util.buttons.GamepadButton
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.attachment.Launcher
import org.firstinspires.ftc.teamcode.attachment.Storage
import org.firstinspires.ftc.teamcode.robot.Gabe
import org.firstinspires.ftc.teamcode.robot.GabePP
import org.firstinspires.ftc.teamcode.util.AllianceColor

@TeleOp(name = "Manatee", group = "Competition")
class Manatee :
    SimpleManualModeWithSpeedModes<MecanumDriveWithPP, Gabe<MecanumDriveWithPP>>({
        GabePP(it, blackboard["endPose"] as? Pose ?: Pose())
    }) {
    // --- State ---

    var targetVelocity = 1500.0
    var modifyScale = 100.0

    val allianceColor: AllianceColor by lazy {
        blackboard["allianceColor"] as? AllianceColor ?: AllianceColor.BLUE
    }

    // --- Controls ---

    private val Launcher.controls
        get() = controls {
            GamepadButton.RIGHT_BUMPER2.onTap { +enable(targetVelocity) }
            GamepadButton.RIGHT_BUMPER2.onRelease { +disable() }
            GamepadButton.DPAD_UP2.onRelease {
                instant { targetVelocity += modifyScale }
                if (!gamepad2.isRumbling) gamepad2.rumble(0.5, 0.5, 100)
            }
            GamepadButton.DPAD_DOWN2.onRelease {
                instant { targetVelocity -= modifyScale }
                if (!gamepad2.isRumbling) gamepad2.rumble(0.5, 0.5, 100)
            }
            GamepadButton.DPAD_RIGHT2.onRelease { instant { modifyScale *= 10 } }
            GamepadButton.DPAD_LEFT2.onRelease { instant { modifyScale /= 10 } }
        }

    private val Storage.controls
        get() = controls {
            GamepadButton.A2.onTap { +release() }
            GamepadButton.A2.onRelease { +close() }
        }

    private val aimingControls = controls {
        GamepadButton.RIGHT_BUMPER1.onTap {
            context(telemetry) { +robot.pointTowardsAprilTag(allianceColor) }
        }
    }

    override val extraControls: ControlScope<Gabe<MecanumDriveWithPP>>.() -> Unit = {
        robot.launcher.controls
        robot.storage.controls
        aimingControls
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
