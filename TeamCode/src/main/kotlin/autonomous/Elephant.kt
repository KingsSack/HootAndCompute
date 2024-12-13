package org.firstinspires.ftc.teamcode.autonomous

import com.acmerobotics.roadrunner.*
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
 * @property controller the autonomous controller
 * @property robot the robot
 *
 * @see AutonomousMode
 */
class Elephant(
    hardwareMap: HardwareMap,
    telemetry: Telemetry,
    params: ElephantParams
) : AutonomousMode {
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
        var initialY: Double = 66.0
        @JvmField
        var initialHeading: Double = 0.0

        @JvmField
        var isPreloaded: Boolean = true

        @JvmField
        var numSamples: Int = 3
    }

    override val controller = AutonomousController(telemetry)
    override val robot = Steve(hardwareMap, Pose2d(
        Vector2d(params.initialX, params.initialY),
        Math.toRadians(params.initialHeading)
    ))

    private var currentSampleIndex = 0

    init {
        // Autonomous sequence
        if (params.isPreloaded) {
            // If preloaded, deposit the preloaded sample
            controller.addAction { goToBasket() }
        }
        repeat(params.numSamples) {
            // Collect samples
            controller.addAction { goToSample() }
            controller.addAction { goToBasket() }
        }
        // Park
        controller.addAction { goToObservationZone() }
    }

    private fun goToSample(): Action {
        return SequentialAction(
            robot.drive.actionBuilder(robot.drive.pose)
                .strafeTo(Vector2d(24.0, 60.0))
                .strafeTo(Vector2d(FieldParams.samplePositionsX[currentSampleIndex] - 8.0, FieldParams.samplePositionsY[currentSampleIndex]))
                .build(),
            InstantAction { currentSampleIndex++ }
        )
    }

    private fun goToBasket(): Action {
        return robot.drive.actionBuilder(robot.drive.pose)
            .strafeTo(Vector2d(FieldParams.basketX, FieldParams.basketY))
            .build()
    }

    private fun goToObservationZone(): Action {
        return robot.drive.actionBuilder(robot.drive.pose)
            .lineToY(36.0)
            .setTangent(Math.toRadians(0.0))
            .lineToX(FieldParams.observationX)
            .setTangent(Math.toRadians(-90.0))
            .lineToY(FieldParams.observationY)
            .build()
    }

    override fun run() {
        controller.execute()
    }
}