package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.roadrunner.*
import com.lasteditguild.volt.autonomous.AutonomousMode
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.util.FieldParams
import org.firstinspires.ftc.teamcode.robot.Steve

/**
 * Elephant is an autonomous mode that pushes samples under the basket.
 *
 * @param hardwareMap the hardware map
 * @param telemetry the telemetry
 * @param params the parameters for Elephant
 *
 * @property robot the robot
 *
 * @see AutonomousMode
 */
class Elephant(
    hardwareMap: HardwareMap,
    telemetry: Telemetry,
    params: ElephantParams
) : AutonomousMode(telemetry) {
    /**
     * The parameters for Elephant.
     *
     * @property initialX the initial x position
     * @property initialY the initial y position
     * @property initialHeading the initial heading
     * @property isPreloaded whether the robot is preloaded
     * @property numSamples the number of samples to collect
     */
    class ElephantParams {
        @JvmField
        var initialX: Double = 24.0
        @JvmField
        var initialY: Double = 63.0
        @JvmField
        var initialHeading: Double = 90.0

        @JvmField
        var isPreloaded: Boolean = true

        @JvmField
        var numSamples: Int = 2
    }

    override val robot = Steve(hardwareMap, Pose2d(
        Vector2d(params.initialX, params.initialY),
        Math.toRadians(params.initialHeading)
    ))

    private var currentSampleIndex = 0

    init {
        // Autonomous sequence
        if (params.isPreloaded) {
            // If preloaded, deposit the preloaded sample
            actionSequence.add { goToBasket() }
        }
        repeat(params.numSamples) {
            // Collect samples
            actionSequence.add { goToSample() }
            actionSequence.add { goToBasket() }
        }
        // Park
        actionSequence.add { goToObservationZone() }
    }

    private fun goToSample(): Action {
        return SequentialAction(
            robot.strafeTo(Vector2d(37.0, 46.0)),
            robot.strafeTo(Vector2d(37.0, FieldParams.samplePositionsY[currentSampleIndex] - 12.0)),
            robot.strafeTo(Vector2d(FieldParams.samplePositionsX[currentSampleIndex], FieldParams.samplePositionsY[currentSampleIndex] - 12.0)),
            InstantAction { currentSampleIndex++ }
        )
    }

    private fun goToBasket(): Action {
        return robot.strafeTo(Vector2d(FieldParams.basketX + 4, FieldParams.basketY + 4))
    }

    private fun goToObservationZone(): Action {
        return robot.driveActionBuilder(robot.pose)
            .strafeTo(Vector2d(24.0, 38.0))
            .setTangent(Math.toRadians(0.0))
            .lineToX(FieldParams.observationX)
            .setTangent(Math.toRadians(-90.0))
            .lineToY(FieldParams.observationY)
            .build()
    }
}