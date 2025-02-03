package org.firstinspires.ftc.teamcode.opmode.manual

import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.Vector2d
import dev.kingssack.volt.manual.SimpleManualModeWithSpeedModes
import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.attachment.Lift
import org.firstinspires.ftc.teamcode.robot.Steve

/**
 * Whale is a manual mode focused on functionality
 *
 * @param hardwareMap the hardware map
 * @param telemetry the telemetry
 * @param gamepad1 gamepad for movement
 * @param gamepad2 gamepad for actions
 *
 * @property robot the robot
 */
class Whale(
    hardwareMap: HardwareMap,
    telemetry: Telemetry,
    private val gamepad1: Gamepad,
    private val gamepad2: Gamepad,
    private val params: WhaleParams = WhaleParams()
) : SimpleManualModeWithSpeedModes(telemetry) {
    /**
     * ManualParams is a configuration object for manual control.
     *
     * @property initialX the initial x position
     * @property initialY the initial y position
     * @property initialHeading the initial heading
     */
    class WhaleParams(
        val initialX: Double = -48.0,
        val initialY: Double = 64.0,
        val initialHeading: Double = 90.0
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

        // Run actions
        runActions()

        // Update robot
        robot.update(telemetry)
    }

    private fun quickMovements(gamepad: Gamepad) {
        // Quick movements
        if (gamepad.dpad_up) runningActions.add(robot.turnTo(Math.toRadians(-90.0)))
        else if (gamepad.dpad_left) runningActions.add(robot.turnTo(Math.toRadians(45.0)))
    }

    private fun controlLiftWithGamepad(gamepad: Gamepad) {
        // Control lifters
        if (gamepad.right_bumper) runningActions.add(robot.lift.goTo(Lift.upperBasketHeight))
        else if (gamepad.left_bumper) runningActions.add(robot.lift.drop())
    }

    private fun controlClawWithGamepad(gamepad: Gamepad) {
        // Control claw
        if (gamepad.a)
            runningActions.add(robot.claw.open())
        if (gamepad.b)
            runningActions.add(robot.claw.close())
    }

    private fun rotateClawWithGamepad(gamepad: Gamepad) {
        // Control claw rotation
        if (gamepad.dpad_right) runningActions.add(robot.wrist.goTo(0.5))
        else if (gamepad.dpad_up) runningActions.add(robot.wrist.goTo(robot.wrist.getPosition() + 0.05))
        else if (gamepad.dpad_down) runningActions.add(robot.wrist.goTo(robot.wrist.getPosition() - 0.05))
    }

    private fun extendClawWithGamepad(gamepad: Gamepad) {
        // Control shoulder and elbow
        if (gamepad.x) runningActions.add(robot.retractArm())
        else if (gamepad.y) runningActions.add(robot.extendArm())
        else {
            robot.elbow.setPower(-gamepad.right_stick_y.toDouble())
        }
    }
}