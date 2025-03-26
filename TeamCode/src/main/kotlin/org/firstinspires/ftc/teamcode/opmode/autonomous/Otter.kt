package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.*
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import dev.kingssack.volt.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.attachment.Lift
import org.firstinspires.ftc.teamcode.util.FieldParams
import org.firstinspires.ftc.teamcode.robot.Steve

/**
 * Otter is an autonomous mode that collects samples and deposits them in the basket.
 *
 * @property robot the robot
 *
 * @see AutonomousMode
 */
@Config
@Autonomous(name = "Otter", group = "Competition")
class Otter : AutonomousMode() {
    /**
     * The parameters for Otter.
     *
     * @property INITIAL_X the initial x position
     * @property INITIAL_Y the initial y position
     * @property INITIAL_HEADING the initial heading
     * @property IS_PRELOADED whether the robot is preloaded
     * @property NUM_SAMPLES the number of samples to collect
     */
    companion object OtterParams {
        @JvmField
        var INITIAL_X: Double = 24.0
        @JvmField
        var INITIAL_Y: Double = 63.0
        @JvmField
        var INITIAL_HEADING: Double = -90.0

        @JvmField
        var IS_PRELOADED: Boolean = true

        @JvmField
        var NUM_SAMPLES: Int = 2
    }

    override val robot = Steve(hardwareMap, Pose2d(
        Vector2d(INITIAL_X, INITIAL_Y),
        Math.toRadians(INITIAL_HEADING)
    ))

    private var currentSampleIndex = 0

    init {
        if (IS_PRELOADED) {
            // If preloaded, deposit the preloaded sample
            actionSequence.add { goToBasket() }
            actionSequence.add { robot.depositSample(Lift.upperBasketHeight) }
        }
        repeat(NUM_SAMPLES) {
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