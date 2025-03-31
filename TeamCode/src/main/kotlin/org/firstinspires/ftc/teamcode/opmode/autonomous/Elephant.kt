package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.*
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.robot.Steve
import org.firstinspires.ftc.teamcode.util.FieldParams

/**
 * Elephant is an autonomous mode that pushes samples under the basket.
 *
 * @property robot the robot
 *
 * @see AutonomousMode
 */
@Config
@Autonomous(name = "Elephant", group = "Competition")
class Elephant : AutonomousMode<Steve>() {
    /**
     * The parameters for Elephant.
     *
     * @property INITIAL_X the initial x position
     * @property INITIAL_Y the initial y position
     * @property INITIAL_HEADING the initial heading
     * @property IS_PRELOADED whether the robot is preloaded
     * @property NUM_SAMPLES the number of samples to collect
     */
    companion object ElephantParams {
        @JvmField
        var INITIAL_X: Double = 24.0
        @JvmField
        var INITIAL_Y: Double = 63.0
        @JvmField
        var INITIAL_HEADING: Double = 90.0

        @JvmField
        var IS_PRELOADED: Boolean = true

        @JvmField
        var NUM_SAMPLES: Int = 2
    }

    override fun createRobot(hardwareMap: HardwareMap): Steve {
        return Steve(hardwareMap, Pose2d(
            Vector2d(INITIAL_X, INITIAL_Y),
            Math.toRadians(INITIAL_HEADING)
        ))
    }

    private var currentSampleIndex = 0

    init {
        if (IS_PRELOADED) {
            // If preloaded, deposit the preloaded sample
            actionSequence.add { goToBasket() }
        }
        repeat(NUM_SAMPLES) {
            // Collect samples
            actionSequence.add { goToSample() }
            actionSequence.add { goToBasket() }
        }
        // Park
        actionSequence.add { goToObservationZone() }
    }

    private fun goToSample(): Action {
        return SequentialAction(
            robot.driveActionBuilder(robot.pose)
                .strafeTo(Vector2d(37.0, 46.0))
                .strafeTo(Vector2d(37.0, FieldParams.samplePositionsY[currentSampleIndex] - 12.0))
                .strafeTo(Vector2d(FieldParams.samplePositionsX[currentSampleIndex], FieldParams.samplePositionsY[currentSampleIndex] - 12.0))
                .build(),
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