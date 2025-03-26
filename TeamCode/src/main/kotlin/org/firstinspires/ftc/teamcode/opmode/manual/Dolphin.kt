package org.firstinspires.ftc.teamcode.opmode.manual

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.InstantAction
import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import dev.kingssack.volt.manual.SimpleManualModeWithSpeedModes
import org.firstinspires.ftc.teamcode.attachment.Lift
import org.firstinspires.ftc.teamcode.attachment.Wrist
import org.firstinspires.ftc.teamcode.robot.Steve

/**
 * Dolphin is a manual mode that is highly optimized
 *
 * @property robot the robot
 */
@Config
@TeleOp(name = "Dolphin", group = "Competition")
class Dolphin : SimpleManualModeWithSpeedModes() {
    /**
     * DolphinParams is a configuration object for manual control.
     *
     * @property INITIAL_X the initial x position
     * @property INITIAL_Y the initial y position
     * @property INITIAL_HEADING the initial heading
     * @property LIFT_MULTIPLIER the lift multiplier
     */
    companion object DolphinParams {
        @JvmField
        var INITIAL_X: Double = -48.0
        @JvmField
        var INITIAL_Y: Double = 64.0
        @JvmField
        var INITIAL_HEADING: Double = -90.0
        @JvmField
        var LIFT_MULTIPLIER: Int = 12
    }

    override val robot = Steve(hardwareMap, Pose2d(
        Vector2d(INITIAL_X, INITIAL_Y),
        Math.toRadians(INITIAL_HEADING)
    ))

    private val quickTurnTowardBaskets = Interaction({ isButtonTapped("dpad_left1") },
        { robot.turnTo(Math.toRadians(45.0)) })
    private val quickTurnTowardSubmersible = Interaction({ isButtonTapped("dpad_up1") },
        { robot.turnTo(Math.toRadians(-90.0)) })
    private val quickDepositSpecimen = Interaction({ isButtonTapped("dpad_right1") },
        { robot.depositSpecimen(Lift.upperSubmersibleBarHeight) })

    private val controlLift = Interaction({ true },
        { InstantAction { robot.lift.currentGoal += -getAnalogValue("left_stick_y2").toInt() * LIFT_MULTIPLIER } })
    /* private val resetLift = Interaction({ isButtonTapped("left_stick_button") },
        { InstantAction { robot.lift.reset() } }) */

    private val openClaw = Interaction({ isButtonTapped("a2") }, { robot.claw.open() })
    private val closeClaw = Interaction({ isButtonTapped("b2") }, { robot.claw.close() })

    private val centerWrist = Interaction({ isButtonTapped("dpad_down2") }, { robot.wrist.goTo(Wrist.centerPosition) })
    private val rotateWristLeft = Interaction({ isButtonPressed("dpad_left2") },
        { robot.wrist.goTo(robot.wrist.getPosition() + 0.005) })
    private val rotateWristRight = Interaction({ isButtonPressed("dpad_right2") },
        { robot.wrist.goTo(robot.wrist.getPosition() - 0.005) })

    private val toggleShoulder = ToggleInteraction({ isButtonTapped("y2") },
        { robot.shoulder.extend() }, { robot.shoulder.retract() })

    private val controlElbow = Interaction({ true },
        { InstantAction { robot.elbow.setPower(-getAnalogValue("right_stick_y2")) } })
    private val extendElbow = Interaction({ isButtonTapped("right_stick_button2") }, { robot.elbow.extend() })

    private val extendArm = Interaction({ isButtonTapped("dpad_up2") }, { robot.extendArm() })

    private val extendTail = ToggleInteraction({ isButtonTapped("left_bumper2") }, { robot.tail.extend() }, { robot.tail.center() })
    private val retractTail = ToggleInteraction({ isButtonTapped("right_bumper2") }, { robot.tail.retract() }, { robot.tail.center() })

    private val toggleIntake = ToggleInteraction({ isButtonTapped("x2") },
        { robot.intake.enableIntake() }, { robot.intake.reverseIntake() })
    private val disableIntake = Interaction({ isButtonHeld("x2", 200.0) }, { robot.intake.disableIntake() })

    init {
        interactions.addAll(listOf(
            quickTurnTowardBaskets,
            quickTurnTowardSubmersible,
            quickDepositSpecimen,
            // quickRetrieveSpecimen,
            controlLift,
            // resetLift,
            openClaw,
            closeClaw,
            centerWrist,
            rotateWristLeft,
            rotateWristRight,
            toggleShoulder,
            controlElbow,
            extendElbow,
            extendArm,
            extendTail,
            retractTail,
            toggleIntake,
            disableIntake
        ))
    }

    override fun tick() {
        robot.setDrivePowers(calculatePoseWithGamepad())
        super.tick()
    }
}