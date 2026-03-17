package org.firstinspires.ftc.teamcode.opmode.manual

import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.hardware.Gamepad
import dev.kingssack.volt.attachment.drivetrain.MecanumDriveWithPP
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.opmode.autonomous.AllianceColor
import dev.kingssack.volt.opmode.manual.SimpleManualModeWithSpeedModes
import dev.kingssack.volt.util.Event.ManualEvent.*
import dev.kingssack.volt.util.buttons.AnalogInput
import dev.kingssack.volt.util.buttons.Button
import org.firstinspires.ftc.teamcode.attachment.Classifier
import org.firstinspires.ftc.teamcode.attachment.Launcher
import org.firstinspires.ftc.teamcode.attachment.Pusher
import org.firstinspires.ftc.teamcode.robot.Jones
import org.firstinspires.ftc.teamcode.robot.JonesPP

@VoltOpModeMeta("Seahorse", "Competition")
class Seahorse : SimpleManualModeWithSpeedModes<MecanumDriveWithPP, JonesPP>() {
    override val robot = JonesPP(hardwareMap, blackboard["endPose"] as? Pose ?: Pose())

    // --- State ---

    var targetVelocity = Jones.launcherTargetVelocity
    val allianceColor = blackboard["allianceColor"] as? AllianceColor ?: AllianceColor.BLUE

    var position: Int = 0

    // --- Controls ---

    private fun Launcher.defineControls() {
        Tap(Button.RIGHT_BUMPER2) then { +enable(targetVelocity) }
        Tap(Button.LEFT_BUMPER2) then { +disable() }
        Tap(Button.DPAD_LEFT2) then
            {
                instant { targetVelocity = Jones.launcherLowVelocity }
                if (currentVelocity > 0.0) +enable(targetVelocity)
            }
        Tap(Button.DPAD_UP2) then
            {
                instant { targetVelocity = Jones.launcherMediumVelocity }
                if (currentVelocity > 0.0) +enable(targetVelocity)
            }
        Tap(Button.DPAD_RIGHT2) then
            {
                instant { targetVelocity = Jones.launcherTargetVelocity }
                if (currentVelocity > 0.0) +enable(targetVelocity)
            }
    }

    private fun Classifier.defineControls() {
        Tap(Button.A2) then { +releaseArtifact(Classifier.ReleaseType.NEXT) }
        Tap(Button.X2) then { +releaseArtifact(Classifier.ReleaseType.PURPLE) }
        Tap(Button.Y2) then { +releaseArtifact(Classifier.ReleaseType.GREEN) }
        Tap(Button.DPAD_DOWN1) then
            {
                instant { position++ }
                +goToPos(position % 3 + 1)
            }
    }

    private fun Pusher.defineControls() {
        Tap(Button.B2) then { +push() }
        Release(Button.B2) then { +retract() }
    }

    private fun defineAimingControls() {
        Change(AnalogInput.RIGHT_TRIGGER1) then
            { value ->
                instant {
                    context(telemetry) {
                        if (value <= 0.3) {
                            robot.aprilTagAiming.reset()
                            return@instant
                        }

                        val targetId = if (allianceColor == AllianceColor.BLUE) 20 else 24
                        val tag = robot.getDetectedAprilTags(targetId).firstOrNull()

                        rx =
                            if (tag != null) {
                                robot.aprilTagAiming.pointTowardsAprilTag(tag)
                            } else {
                                robot.aprilTagAiming.reset()
                                0.0
                            }
                    }
                }
            }
    }

    private fun defineAutoFireControls() {
        Tap(Button.DPAD_DOWN2) then { +robot.fireAllStoredArtifacts(targetVelocity) }
    }

    init {
        robot.launcher.defineControls()
        robot.classifier.defineControls()
        robot.pusher.defineControls()
        defineAimingControls()
        defineAutoFireControls()
    }

    init {
        robot.drivetrain.startTeleOpDrive()
    }

    override fun tick() {
        with(telemetry) {
            addData("Alliance Color", allianceColor)
            addData("Target Velocity", targetVelocity)
            addData("Classifier Position", position % 3 + 1)
        }

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
