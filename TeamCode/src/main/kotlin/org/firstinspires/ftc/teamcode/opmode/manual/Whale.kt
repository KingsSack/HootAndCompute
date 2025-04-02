package org.firstinspires.ftc.teamcode.opmode.manual

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.InstantAction
import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.manual.SimpleManualModeWithSpeedModes
import dev.kingssack.volt.util.GamepadAnalogInput
import dev.kingssack.volt.util.GamepadButton
import org.firstinspires.ftc.teamcode.attachment.Lift
import org.firstinspires.ftc.teamcode.attachment.Wrist
import org.firstinspires.ftc.teamcode.robot.Steve

/**
 * Whale is a manual mode focused on capability
 *
 * @property robot the robot
 */
@Config
@TeleOp(name = "Whale", group = "Competition")
class Whale : SimpleManualModeWithSpeedModes<Steve>() {
    /**
     * WhaleParams is a configuration object for manual control.
     *
     * @property INITIAL_X the initial x position
     * @property INITIAL_Y the initial y position
     * @property INITIAL_HEADING the initial heading
     * @property LIFT_MULTIPLIER the lift multiplier
     */
    companion object WhaleParams {
        @JvmField
        var INITIAL_X: Double = -48.0
        @JvmField
        var INITIAL_Y: Double = 64.0
        @JvmField
        var INITIAL_HEADING: Double = 90.0
        @JvmField
        var LIFT_MULTIPLIER: Int = 12
    }

    override fun createRobot(hardwareMap: HardwareMap): Steve {
        return Steve(hardwareMap, Pose2d(
            Vector2d(INITIAL_X, INITIAL_Y),
            Math.toRadians(INITIAL_HEADING)
        ))
    }

    private val quickTurnTowardBaskets = Interaction({ isButtonTapped(GamepadButton.DPAD_LEFT1) },
        { robot.turnTo(Math.toRadians(45.0)) })
    private val quickTurnTowardSubmersible = Interaction({ isButtonTapped(GamepadButton.DPAD_UP1) }, { robot.turnTo(Math.toRadians(-90.0)) })
    private val quickDepositSample = Interaction({ isButtonTapped(GamepadButton.DPAD_RIGHT1) },
        { robot.depositSample(Lift.upperBasketHeight) })

    private val raiseLift = Interaction({ isButtonTapped(GamepadButton.RIGHT_BUMPER2) }, { robot.lift.goTo(Lift.upperBasketHeight) })
    private val lowerLift = Interaction({ isButtonTapped(GamepadButton.LEFT_BUMPER2) }, { robot.lift.drop() })
    private val controlLift = Interaction({ true },
        { InstantAction { robot.lift.currentGoal += -getAnalogValue(GamepadAnalogInput.LEFT_STICK_Y2).toInt() * LIFT_MULTIPLIER } })

    private val openClaw = Interaction({ isButtonTapped(GamepadButton.A2) }, { robot.claw.open() })
    private val closeClaw = Interaction({ isButtonTapped(GamepadButton.B2) }, { robot.claw.close() })

    private val centerWrist = Interaction({ isButtonTapped(GamepadButton.DPAD_DOWN2) }, { robot.wrist.goTo(Wrist.centerPosition) })
    private val rotateWristLeft = Interaction({ isButtonPressed(GamepadButton.DPAD_LEFT2) },
        { robot.wrist.goTo(robot.wrist.getPosition() + 0.005) })
    private val rotateWristRight = Interaction({ isButtonPressed(GamepadButton.DPAD_RIGHT2) },
        { robot.wrist.goTo(robot.wrist.getPosition() - 0.005) })

    private val extendArm = Interaction({ isButtonTapped(GamepadButton.X2) }, { robot.extendArm() })
    private val retractArm = Interaction({ isButtonTapped(GamepadButton.Y2) }, { robot.retractArm() })

    private val controlElbow = Interaction({ true },
        { InstantAction { robot.elbow.setPower(-getAnalogValue(GamepadAnalogInput.RIGHT_STICK_Y2)) } })

    init {
        interactions.addAll(listOf(
            quickTurnTowardBaskets,
            quickTurnTowardSubmersible,
            quickDepositSample,
            raiseLift,
            lowerLift,
            controlLift,
            openClaw,
            closeClaw,
            centerWrist,
            rotateWristLeft,
            rotateWristRight,
            extendArm,
            retractArm,
            controlElbow
        ))
    }

    override fun tick() {
        robot.setDrivePowers(calculatePoseWithGamepad())
        super.tick()
    }
}