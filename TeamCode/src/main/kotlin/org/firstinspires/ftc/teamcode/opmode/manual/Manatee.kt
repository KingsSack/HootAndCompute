package org.firstinspires.ftc.teamcode.opmode.manual

import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.Gamepad
import dev.kingssack.volt.attachment.drivetrain.MecanumDriveWithPP
import dev.kingssack.volt.opmode.manual.SimpleManualModeWithSpeedModes
import dev.kingssack.volt.util.Event.ManualEvent.*
import dev.kingssack.volt.util.buttons.Button
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

    private fun Launcher.controls() {
        Release(Button.RIGHT_BUMPER2) then { +enable(targetVelocity) }
        Release(Button.LEFT_BUMPER2) then { +disable() }
        Release(Button.DPAD_UP2) then
            {
                instant { targetVelocity += modifyScale }
                if (!gamepad2.isRumbling) gamepad2.rumble(0.5, 0.5, 100)
            }
        Release(Button.DPAD_DOWN2) then
            {
                instant { targetVelocity -= modifyScale }
                if (!gamepad2.isRumbling) gamepad2.rumble(0.5, 0.5, 100)
            }
        Release(Button.DPAD_RIGHT2) then { instant { modifyScale *= 10 } }
        Release(Button.DPAD_LEFT2) then { instant { modifyScale /= 10 } }
    }

    private fun Storage.controls() {
        Tap(Button.A2) then { +release() }
        Release(Button.A2) then { +close() }
    }

    private fun aimingControls() {
        Tap(Button.RIGHT_BUMPER1) then
            {
                context(telemetry) { +robot.pointTowardsAprilTag(allianceColor) }
            }
    }

    override fun defineEvents() {
        super.defineEvents()
        robot.launcher.controls()
        robot.storage.controls()
        aimingControls()
    }

    override fun initialize() {
        super.initialize()
        robot.drivetrain.startTeleOpDrive()
    }

    override fun tick() {
        with(telemetry) {
            addData("Alliance Color", allianceColor)
            addData("Target Velocity", targetVelocity)
            addData("Modify Scale", modifyScale)
        }

        if (robot.launcher.isAtSpeed && robot.launcher.currentVelocity > 0.0) {
            gamepad2.setLedColor(0.0, 1.0, 0.0, Gamepad.LED_DURATION_CONTINUOUS)
            gamepad2.rumble(1.0, 1.0, Gamepad.RUMBLE_DURATION_CONTINUOUS)
        } else {
            gamepad2.setLedColor(1.0, 0.0, 0.0, Gamepad.LED_DURATION_CONTINUOUS)
        }

        super.tick()
    }
}
