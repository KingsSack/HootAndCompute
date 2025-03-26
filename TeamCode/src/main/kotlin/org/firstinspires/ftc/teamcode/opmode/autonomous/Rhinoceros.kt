package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.*
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import dev.kingssack.volt.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.attachment.Lift
import org.firstinspires.ftc.teamcode.robot.Steve
import org.firstinspires.ftc.teamcode.util.FieldParams

/**
 * Rhinoceros is an autonomous mode that places specimens on the top bar of the submersible.
 *
 * @property robot the robot
 *
 * @see AutonomousMode
 */
@Config
@Autonomous(name = "Rhinoceros", group = "Competition")
class Rhinoceros : AutonomousMode() {
    /**`
     * The parameters for Rhinoceros.
     *
     * @property INITIAL_X the initial x position
     * @property INITIAL_Y the initial y position
     * @property INITIAL_HEADING the initial heading
     * @property NUM_SAMPLES the number of samples to convert to a specimen
     */
    companion object RhinocerosParams {
        @JvmField
        var INITIAL_X: Double = -24.0
        @JvmField
        var INITIAL_Y: Double = 63.0
        @JvmField
        var INITIAL_HEADING: Double = -90.0

        @JvmField
        var NUM_SAMPLES: Int = 1
    }

    override val robot = Steve(hardwareMap, Pose2d(
        Vector2d(INITIAL_X, INITIAL_Y),
        Math.toRadians(INITIAL_HEADING)
    ))

    private var currentSampleIndex = 0
    private var currentSubmersiblePosition = Vector2d(FieldParams.submersibleX, FieldParams.submersibleY + 14)

    init {
        actionSequence.add { initialize() }
        // Deposit the preloaded specimen
        actionSequence.add { goToSubmersible() }
        actionSequence.add { depositSpecimen() }
        repeat(NUM_SAMPLES) {
            // Collect samples
            actionSequence.add { getSample() }
            actionSequence.add { retrieveSpecimen() }
            actionSequence.add { goToSubmersible() }
            actionSequence.add { depositSpecimen() }
        }
        // Park
        actionSequence.add { goToObservationZone() }
    }

    private fun initialize(): Action {
        return robot.tail.retract()
    }

    private fun goToSubmersible(): Action {
        return robot.strafeTo(currentSubmersiblePosition)
    }

    private fun depositSpecimen(): Action {
        return SequentialAction(
            robot.depositSpecimen(Lift.upperSubmersibleBarHeight),
            InstantAction { currentSubmersiblePosition = Vector2d(currentSubmersiblePosition.x + 5.0, currentSubmersiblePosition.y) },
        )
    }

    private fun getSample(): Action {
        return SequentialAction(
            robot.driveActionBuilder(robot.pose)
                .strafeToLinearHeading(Vector2d(-37.0, 42.0), Math.toRadians(90.0))
                .strafeTo(Vector2d(-37.0, FieldParams.samplePositionsY[currentSampleIndex] - 14.0))
                .strafeTo(Vector2d(-FieldParams.samplePositionsX[currentSampleIndex], FieldParams.samplePositionsY[currentSampleIndex] - 12.0))
                .strafeTo(Vector2d(-FieldParams.samplePositionsX[currentSampleIndex], FieldParams.observationY))
                .build(),
        )
    }

    private fun retrieveSpecimen(): Action {
        return SequentialAction(
            robot.strafeTo(Vector2d(-FieldParams.samplePositionsX[currentSampleIndex], 44.0)),
            robot.retrieveSpecimen(),
            InstantAction { currentSampleIndex++ },
            robot.turnTo(Math.toRadians(-90.0))
        )
    }

    private fun goToObservationZone(): Action {
        return robot.strafeTo(Vector2d(FieldParams.observationX, FieldParams.observationY))
    }
}