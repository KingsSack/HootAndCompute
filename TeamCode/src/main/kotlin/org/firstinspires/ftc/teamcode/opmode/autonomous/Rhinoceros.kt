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
    /**
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

        val numSamples: Int = 3
    )

    override val robot = Steve(hardwareMap, Pose2d(
        Vector2d(params.initialX, params.initialY),
        Math.toRadians(params.initialHeading)
    ))

    private var currentSampleIndex = 0
    private var currentSubmersiblePosition = Vector2d(FieldParams.submersibleX, FieldParams.submersibleY + 30)

    init {
        actionSequence.add { goToSubmersible() }
        actionSequence.add { depositSpecimen() } // Deposit the preloaded specimen
        repeat(params.numSamples) {
            // Collect samples
            actionSequence.add { goToSample() }
            actionSequence.add { goToObservationZone() }
            actionSequence.add { retrieveSpecimen() }
            actionSequence.add { goToSubmersible() }
            actionSequence.add { depositSpecimen() }
        }
        // Park
        actionSequence.add { goToObservationZone() }
    }

    private fun goToSample(): Action {
        return SequentialAction(
            robot.driveActionBuilder(robot.pose)
                .strafeTo(Vector2d(-36.0, 42.0))
                .splineToLinearHeading(Pose2d(Vector2d(-FieldParams.samplePositionsX[currentSampleIndex], FieldParams.samplePositionsY[currentSampleIndex] - 16.0), Math.toRadians(90.0)), 0.0)
                .build(),
            InstantAction { currentSampleIndex++ }
        )
    }

    private fun goToSubmersible(): Action {
        return robot.strafeTo(currentSubmersiblePosition)
    }

    private fun depositSpecimen(): Action {
        return SequentialAction(
            robot.lift.goTo(Lift.upperSubmersibleBarHeight),
            robot.extendArmToSubmersible(),
            robot.lift.drop(),
            InstantAction { currentSubmersiblePosition = Vector2d(currentSubmersiblePosition.x + 6.0, currentSubmersiblePosition.y) },
        )
    }

    private fun goToObservationZone(): Action {
        return robot.strafeTo(Vector2d(FieldParams.observationX, FieldParams.observationY))
    }

    private fun retrieveSpecimen(): Action {
        return SequentialAction(
            robot.strafeTo(Vector2d(FieldParams.observationX, 33.0)),
            robot.extendArm(),
            robot.claw.close(),
            robot.retractArm()
        )
    }
}