package org.firstinspires.ftc.teamcode.opmode.manual

import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.Vector2d
import dev.kingssack.volt.manual.SimpleManualModeWithSpeedModes
import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.attachment.Lift
import org.firstinspires.ftc.teamcode.attachment.Wrist
import org.firstinspires.ftc.teamcode.robot.Steve

/**
 * Dolphin is a manual mode that is highly optimized
 *
 * @param hardwareMap the hardware map
 * @param telemetry the telemetry
 * @param gamepad1 gamepad for movement
 * @param gamepad2 gamepad for actions
 *
 * @property robot the robot
 */
class Dolphin(
    hardwareMap: HardwareMap,
    telemetry: Telemetry,
    private val gamepad1: Gamepad,
    private val gamepad2: Gamepad,
    private val params: DolphinParams = DolphinParams(),
) : SimpleManualModeWithSpeedModes(telemetry) {
    /**
     * ManualParams is a configuration object for manual control.
     *
     * @property initialX the initial x position
     * @property initialY the initial y position
     * @property initialHeading the initial heading
     */
    class DolphinParams(
        val initialX: Double = -48.0,
        val initialY: Double = 64.0,
        val initialHeading: Double = -90.0,

        val liftMultiplier: Int = 12,
        val shoulderMultiplier: Int = 8
    )

    override val robot = Steve(hardwareMap, Pose2d(
        Vector2d(params.initialX, params.initialY),
        Math.toRadians(params.initialHeading)
    ))

    override fun tick(telemetry: Telemetry) {
        // Drive
        robot.setDrivePowers(calculatePoseWithGamepad(gamepad1))
        quickMovements(gamepad1)

        // Control lift and claw
        controlLiftWithGamepad(gamepad2)
        controlClawWithGamepad(gamepad2)
        rotateClawWithGamepad(gamepad2)
        extendClawWithGamepad(gamepad2)

        // Control the intake
        moveIntakeWithGamepad(gamepad2)
        toggleIntakeWithGamepad(gamepad2)

        // Run actions
        runActions()

        // Update robot
        robot.update(telemetry)
    }

    private fun quickMovements(gamepad: Gamepad) {
        if (gamepad.dpad_up) runningActions.add(robot.turnTo(Math.toRadians(-90.0)))
        else if (gamepad.dpad_left) runningActions.add(robot.turnTo(Math.toRadians(45.0)))
        else if (gamepad.dpad_right) runningActions.add(robot.depositSpecimen(Lift.upperSubmersibleBarHeight))
    }

    private fun controlLiftWithGamepad(gamepad: Gamepad) {
        // Check reset
        if (gamepad.left_stick_button) robot.lift.reset()

        // Control lift
        robot.lift.currentGoal += processInput(-gamepad.left_stick_y.toDouble()).toInt() * params.liftMultiplier
    }

    private fun controlClawWithGamepad(gamepad: Gamepad) {
        // Control claw
        if (gamepad.a) runningActions.add(robot.claw.open())
        if (gamepad.b) runningActions.add(robot.claw.close())
    }

    private fun rotateClawWithGamepad(gamepad: Gamepad) {
        // Control wrist
        if (gamepad.dpad_down) runningActions.add(robot.wrist.goTo(Wrist.centerPosition))
        else if (gamepad.dpad_left) runningActions.add(robot.wrist.goTo(robot.wrist.getPosition() + 0.005))
        else if (gamepad.dpad_right) runningActions.add(robot.wrist.goTo(robot.wrist.getPosition() - 0.005))
    }

    private fun extendClawWithGamepad(gamepad: Gamepad) {
        // Control shoulder
        robot.shoulder.currentGoal += (gamepad.right_trigger.toDouble() - gamepad.left_trigger.toDouble()).toInt() * params.shoulderMultiplier

        // Control elbow
        if (gamepad.right_stick_button) runningActions.add(robot.elbow.extend())
        else robot.elbow.setPower(processInput(-gamepad.right_stick_y.toDouble()))

        // Control both
        if (gamepad.dpad_up) runningActions.add(robot.extendArm())
    }

    private fun moveIntakeWithGamepad(gamepad: Gamepad) {
        // Control tail
        if (gamepad.left_bumper) runningActions.add(robot.tail.retract())
        else if (gamepad.right_bumper) runningActions.add(robot.tail.extend())
    }

    private fun toggleIntakeWithGamepad(gamepad: Gamepad) {
        // Control intake
        if (gamepad.x) {
            if (robot.intake.enabled) runningActions.add(robot.intake.disableIntake())
            else runningActions.add(robot.intake.enableIntake())
        }
        if (gamepad.y) {
            if (robot.intake.enabled) runningActions.add(robot.intake.disableIntake())
            else runningActions.add(robot.intake.reverseIntake())
        }
    }
}