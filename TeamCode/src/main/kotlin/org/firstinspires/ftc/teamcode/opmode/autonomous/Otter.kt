package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.roadrunner.*
import com.lasteditguild.volt.autonomous.AutonomousMode
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
    private val params: OtterParams
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
    class OtterParams {
        @JvmField
        var initialX: Double = 24.0
        @JvmField
        var initialY: Double = 63.0
        @JvmField
        var initialHeading: Double = 0.0

        @JvmField
        var angleOfAttack: Double = 45.0

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
        return SequentialAction(
            robot.turnTo(Math.toRadians(-90.0)),
            robot.wait(1.0),
            robot.strafeTo(Vector2d(FieldParams.samplePositionsX[currentSampleIndex], 54.0))
        )
    }

    private fun collectSample(): Action {
        return SequentialAction(
            robot.collectSample(),
            InstantAction { currentSampleIndex++ }
        )
    }

    private fun goToBasket(): Action {
        return SequentialAction(
            robot.strafeTo(Vector2d(FieldParams.basketX, FieldParams.basketY)),
            robot.turnTo(Math.toRadians(params.angleOfAttack))
        )
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