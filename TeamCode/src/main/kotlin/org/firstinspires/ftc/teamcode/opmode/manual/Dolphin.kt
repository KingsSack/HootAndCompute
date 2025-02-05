package org.firstinspires.ftc.teamcode.opmode.manual

import com.acmerobotics.roadrunner.InstantAction
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
    gamepad1: Gamepad,
    gamepad2: Gamepad,
    private val params: DolphinParams = DolphinParams(),
) : SimpleManualModeWithSpeedModes(gamepad1, gamepad2, telemetry) {
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

        val liftMultiplier: Int = 12
    )

    override val robot = Steve(hardwareMap, Pose2d(
        Vector2d(params.initialX, params.initialY),
        Math.toRadians(params.initialHeading)
    ))

    private val quickTurnTowardBaskets = Interaction({ isButtonTapped("dpad_left1") },
        { robot.turnTo(Math.toRadians(45.0)) })
    private val quickTurnTowardSubmersible = Interaction({ isButtonTapped("dpad_up1") },
        { robot.turnTo(Math.toRadians(-90.0)) })
    private val quickDepositSpecimen = Interaction({ isButtonTapped("dpad_right1") },
        { robot.depositSpecimen(Lift.upperSubmersibleBarHeight) })
    private val quickRetrieveSpecimen = Interaction({ isButtonTapped("x1") }, { robot.retrieveSpecimen() })

    private val controlLift = Interaction({ true },
        { InstantAction { robot.lift.currentGoal += -getAnalogValue("left_stick_y2").toInt() * params.liftMultiplier } })

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

    private val toggleIntake = ToggleInteraction({ isButtonTapped("x2") },
        { robot.intake.enableIntake() }, { robot.intake.reverseIntake() })
    private val disableIntake = Interaction({ isButtonHeld("x2", 200.0) }, { robot.intake.disableIntake() })

    init {
        interactions.addAll(listOf(
            quickTurnTowardBaskets,
            quickTurnTowardSubmersible,
            quickDepositSpecimen,
            quickRetrieveSpecimen,
            controlLift,
            openClaw,
            closeClaw,
            centerWrist,
            rotateWristLeft,
            rotateWristRight,
            toggleShoulder,
            controlElbow,
            extendElbow,
            extendArm,
            toggleIntake,
            disableIntake
        ))
    }

    override fun tick(telemetry: Telemetry) {
        // Drive
        robot.setDrivePowers(calculatePoseWithGamepad())
        // quickMovements()

        // Control lift and claw
        /* controlLiftWithGamepad()
        controlClawWithGamepad()
        rotateClawWithGamepad()
        extendClawWithGamepad() */

        // Control the intake
        /* moveIntakeWithGamepad()
        toggleIntakeWithGamepad() */

        super.tick(telemetry)
    }

    /* private fun quickMovements() {
        if (isButtonTapped("dpad_up1")) runningActions.add(robot.turnTo(Math.toRadians(-90.0)))
        else if (isButtonTapped("dpad_left1")) runningActions.add(robot.turnTo(Math.toRadians(45.0)))
        else if (isButtonTapped("dpad_right1")) runningActions.add(robot.depositSpecimen(Lift.upperSubmersibleBarHeight))
        else if (isButtonTapped("x1")) runningActions.add(robot.retrieveSpecimen())
    }

    private fun controlLiftWithGamepad() {
        // Check reset
        if (isButtonTapped("left_stick_button1")) robot.lift.reset()

        // Control lift
        robot.lift.currentGoal += -getAnalogValue("left_stick_y2").toInt() * params.liftMultiplier
    }

    private fun controlClawWithGamepad() {
        // Control claw
        if (isButtonTapped("a2")) runningActions.add(robot.claw.open())
        if (isButtonTapped("b2")) runningActions.add(robot.claw.close())
    }

    private fun rotateClawWithGamepad() {
        // Control wrist
        if (isButtonTapped("dpad_down2")) runningActions.add(robot.wrist.goTo(Wrist.centerPosition))
        else if (isButtonPressed("dpad_left2")) runningActions.add(robot.wrist.goTo(robot.wrist.getPosition() + 0.005))
        else if (isButtonPressed("dpad_right2")) runningActions.add(robot.wrist.goTo(robot.wrist.getPosition() - 0.005))
    }

    private fun extendClawWithGamepad() {
        // Control shoulder
        if (isButtonTapped("y2")) {
            if (robot.shoulder.currentGoal >= Shoulder.maxPosition) runningActions.add(robot.shoulder.retract())
            else runningActions.add(robot.shoulder.extend())
        }

        // Control elbow
        if (isButtonTapped("right_stick_button2")) runningActions.add(robot.elbow.extend())
        else robot.elbow.setPower(-getAnalogValue("right_stick_y2"))

        // Control both
        if (isButtonTapped("dpad_up2")) runningActions.add(robot.extendArm())
    }

    private fun moveIntakeWithGamepad() {
        // Control tail
        if (isButtonTapped("left_bumper2")) runningActions.add(robot.tail.retract())
        else if (isButtonTapped("right_bumper2")) runningActions.add(robot.tail.extend())
    }

    private fun toggleIntakeWithGamepad() {
        // Control intake
        if (isButtonTapped("x2")) {
            if (robot.intake.reversing) runningActions.add(robot.intake.enableIntake())
            else runningActions.add(robot.intake.reverseIntake())
        }
        if (isButtonHeld("x2", 200.0)) runningActions.add(robot.intake.disableIntake())
    } */
}