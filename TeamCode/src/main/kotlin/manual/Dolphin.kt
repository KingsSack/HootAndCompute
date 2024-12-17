package org.firstinspires.ftc.teamcode.manual

import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.attachment.Claw
import org.firstinspires.ftc.teamcode.attachment.Lift
import org.firstinspires.ftc.teamcode.robot.Steve

/**
 * Dolphin is a manual mode that is highly optimized
 *
 * @param hardwareMap the hardware map
 * @param telemetry the telemetry
 * @param gamepad1 gamepad for movement
 * @param gamepad2 gamepad for actions
 *
 * @property controller the manual controller
 * @property robot the robot
 */
class Dolphin(
    hardwareMap: HardwareMap,
    telemetry: Telemetry,
    params: DolphinParams,
    private val gamepad1: Gamepad,
    private val gamepad2: Gamepad
) : ManualMode {
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
    }

    override val controller = ManualController(telemetry)
    override val robot = Steve(hardwareMap, Pose2d(
        Vector2d(params.initialX, params.initialY),
        Math.toRadians(params.initialHeading)
    ))

    override fun tick(telemetry: Telemetry) {
        // Drive
        robot.drive.setDrivePowers(controller.calculatePoseWithGamepad(gamepad1))

        // Control lift and claw
        controlLiftWithGamepad(gamepad2)
        controlClawWithGamepad(gamepad2)
        rotateClawWithGamepad(gamepad2)
        extendClawWithGamepad(gamepad2)

        // Run actions
        controller.runActions()

        // Update robot
        robot.update(telemetry)
    }

    private fun controlLiftWithGamepad(gamepad: Gamepad) {
        // Check reset
        if (gamepad.left_stick_button) robot.lift.reset()

        // Control lift
        if (gamepad.right_bumper) controller.addAction(robot.lift.goTo(Lift.upperBasketHeight))
        else if (gamepad.left_bumper) controller.addAction(robot.lift.drop())
        else robot.lift.setPower(controller.processInput(gamepad.left_stick_y.toDouble()))
    }

    private fun controlClawWithGamepad(gamepad: Gamepad) {
        // Control claw
        robot.claw.setPower(Claw.maxPower * (gamepad.right_trigger - gamepad.left_trigger))
    }

    private fun rotateClawWithGamepad(gamepad: Gamepad) {
        // Control wrist
        if (gamepad.dpad_right) controller.addAction(robot.wrist.twistTo(0.5))
        else if (gamepad.dpad_up) controller.addAction(robot.wrist.twistTo(robot.wrist.getPosition() + 0.05))
        else if (gamepad.dpad_down) controller.addAction(robot.wrist.twistTo(robot.wrist.getPosition() - 0.05))
    }

    private fun extendClawWithGamepad(gamepad: Gamepad) {
        // Control shoulder
        if (gamepad.x) controller.addAction(robot.shoulder.extend())
        else if (gamepad.y) controller.addAction(robot.shoulder.retract())

        // Control elbow
        robot.elbow.setPower(controller.processInput(-gamepad.right_stick_y.toDouble()))
    }
}