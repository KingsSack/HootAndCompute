package org.firstinspires.ftc.teamcode.opmode.manual

import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.Vector2d
import com.lasteditguild.volt.manual.SimpleManualModeWithSpeedModes
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
    private val params: DolphinParams,
    private val gamepad1: Gamepad,
    private val gamepad2: Gamepad
) : SimpleManualModeWithSpeedModes(telemetry) {
    /**
     * ManualParams is a configuration object for manual control.
     *
     * @property initialX the initial x position
     * @property initialY the initial y position
     * @property initialHeading the initial heading
     */
    class DolphinParams {
        @JvmField
        var initialX: Double = -48.0
        @JvmField
        var initialY: Double = 64.0
        @JvmField
        var initialHeading: Double = 90.0

        @JvmField
        var liftMultiplier: Int = 12
        @JvmField
        var shoulderMultiplier: Int = 8
    }

    override val robot = Steve(hardwareMap, Pose2d(
        Vector2d(params.initialX, params.initialY),
        Math.toRadians(params.initialHeading)
    ))

    override fun tick(telemetry: Telemetry) {
        // Drive
        robot.setDrivePowers(calculatePoseWithGamepad(gamepad1))

        // Control lift and claw
        controlLiftWithGamepad(gamepad2)
        controlClawWithGamepad(gamepad2)
        rotateClawWithGamepad(gamepad2)
        extendClawWithGamepad(gamepad2)

        // Run actions
        runActions()

        // Update robot
        robot.update(telemetry)
    }

    private fun controlLiftWithGamepad(gamepad: Gamepad) {
        // Check reset
        if (gamepad.left_stick_button) robot.lift.reset()

        // Control lift
        if (gamepad.right_bumper) runningActions.add(robot.lift.goTo(Lift.upperBasketHeight))
        else if (gamepad.left_bumper) runningActions.add(robot.lift.drop())
        else robot.lift.currentGoal += processInput(-gamepad.left_stick_y.toDouble()).toInt() * params.liftMultiplier
    }

    private fun controlClawWithGamepad(gamepad: Gamepad) {
        // Control claw
        if (gamepad.a)
            runningActions.add(robot.claw.open())
        if (gamepad.b)
            runningActions.add(robot.claw.close())
    }

    private fun rotateClawWithGamepad(gamepad: Gamepad) {
        // Control wrist
        if (gamepad.dpad_down) runningActions.add(robot.wrist.goTo(Wrist.centerPosition))
        else if (gamepad.dpad_left) runningActions.add(robot.wrist.goTo(robot.wrist.getPosition() + 0.005))
        else if (gamepad.dpad_right) runningActions.add(robot.wrist.goTo(robot.wrist.getPosition() - 0.005))
    }

    private fun extendClawWithGamepad(gamepad: Gamepad) {
        // Control shoulder
        if (gamepad.x) runningActions.add(robot.shoulder.extend())
        else if (gamepad.y) runningActions.add(robot.shoulder.retract())
        else robot.shoulder.currentGoal += (gamepad.right_trigger.toDouble() - gamepad.left_trigger.toDouble()).toInt() * params.shoulderMultiplier

        // Control elbow
        if (gamepad.dpad_up) runningActions.add(robot.elbow.extend())
        else robot.elbow.setPower(processInput(-gamepad.right_stick_y.toDouble()))
    }
}