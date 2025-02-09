package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.roadrunner.*
import dev.kingssack.volt.autonomous.AutonomousMode
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.attachment.Lift
import org.firstinspires.ftc.teamcode.util.FieldParams
import org.firstinspires.ftc.teamcode.robot.Steve

/**
 * Otter is an autonomous mode that collects samples and deposits them in the basket.
 *
 * @param hardwareMap the hardware map
 * @param telemetry the telemetry
 * @param params the parameters for Otter
 *
 * @property robot the robot
 *
 * @see AutonomousMode
 */
class Otter(
    hardwareMap: HardwareMap,
    telemetry: Telemetry,
    private val params: OtterParams = OtterParams()
) : AutonomousMode(telemetry) {
    /**
     * The parameters for Otter.
     *
     * @property initialX the initial x position
     * @property initialY the initial y position
     * @property initialHeading the initial heading
     * @property isPreloaded whether the robot is preloaded
     * @property numSamples the number of samples to collect
     */
    class OtterParams(
        val initialX: Double = 24.0,
        val initialY: Double = 63.0,
        val initialHeading: Double = -90.0,

        val isPreloaded: Boolean = true,

        val numSamples: Int = 2
    )

    override val robot = Steve(hardwareMap, Pose2d(
        Vector2d(params.initialX, params.initialY),
        Math.toRadians(params.initialHeading)
    ))

    private var currentSampleIndex = 0

    init {
        if (params.isPreloaded) {
            // If preloaded, deposit the preloaded sample
            actionSequence.add { goToBasket() }
            actionSequence.add { robot.depositSample(Lift.upperBasketHeight) }
        }
        repeat(params.numSamples) {
            // Collect samples
            actionSequence.add { goToSample() }
            actionSequence.add { collectSample() }
            actionSequence.add { goToBasket() }
            actionSequence.add { robot.depositSample(Lift.upperBasketHeight) }
        }
        // Park
        actionSequence.add { goToObservationZone() }
    }

    private fun goToSample(): Action {
        return robot.driveActionBuilder(robot.pose)
            .turn(Math.toRadians(-135.0))
            .strafeTo(Vector2d(FieldParams.samplePositionsX[currentSampleIndex], 45.0))
            .build()
    }

    private fun collectSample(): Action {
        return SequentialAction(
            robot.collectSample(),
            InstantAction { currentSampleIndex++ }
        )
    }

    private fun goToBasket(): Action {
        return robot.strafeToLinearHeading(Vector2d(FieldParams.basketX - 1.0, FieldParams.basketY - 1.0), Math.toRadians(45.0))
    }

    private fun goToObservationZone(): Action {
        return robot.driveActionBuilder(robot.pose)
            .lineToY(36.0)
            .setTangent(Math.toRadians(0.0))
            .lineToX(FieldParams.observationX)
            .setTangent(Math.toRadians(-90.0))
            .lineToY(FieldParams.observationY)
            .build()
    }
}