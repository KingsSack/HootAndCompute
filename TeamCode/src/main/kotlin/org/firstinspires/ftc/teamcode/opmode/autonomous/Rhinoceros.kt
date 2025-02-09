package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.roadrunner.*
import dev.kingssack.volt.autonomous.AutonomousMode
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.attachment.Lift
import org.firstinspires.ftc.teamcode.util.FieldParams
import org.firstinspires.ftc.teamcode.robot.Steve

/**
 * Rhinoceros is an autonomous mode that places specimens on the top bar of the submersible.
 *
 *
 * @param hardwareMap the hardware map
 * @param telemetry the telemetry
 * @param params the parameters for Rhinoceros
 *
 * @property robot the robot
 *
 * @see AutonomousMode
 */
class Rhinoceros(
    hardwareMap: HardwareMap,
    telemetry: Telemetry,
    private val params: RhinocerosParams = RhinocerosParams()
) : AutonomousMode(telemetry) {
    /**`
     * The parameters for Rhinoceros.
     *
     * @property initialX the initial x position
     * @property initialY the initial y position
     * @property initialHeading the initial heading
     * @property numSamples the number of samples to convert to a specimen
     */
    class RhinocerosParams(
        val initialX: Double = -24.0,
        val initialY: Double = 63.0,
        val initialHeading: Double = -90.0,

        val numSamples: Int = 1
    )

    override val robot = Steve(hardwareMap, Pose2d(
        Vector2d(params.initialX, params.initialY),
        Math.toRadians(params.initialHeading)
    ))

    private var currentSampleIndex = 0
    private var currentSubmersiblePosition = Vector2d(FieldParams.submersibleX, FieldParams.submersibleY + 14)

    init {
        actionSequence.add { initialize() }
        // Deposit the preloaded specimen
        actionSequence.add { goToSubmersible() }
        actionSequence.add { depositSpecimen() }
        repeat(params.numSamples) {
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