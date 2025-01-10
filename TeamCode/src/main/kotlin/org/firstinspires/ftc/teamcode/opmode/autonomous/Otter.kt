package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.roadrunner.*
import com.lasteditguild.volt.autonomous.AutonomousController
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
 * @property controller the autonomous controller
 * @property robot the robot
 *
 * @see AutonomousMode
 */
class Otter(
    hardwareMap: HardwareMap,
    telemetry: Telemetry,
    private val params: OtterParams
) : AutonomousMode {
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
        var initialY: Double = 66.0
        @JvmField
        var initialHeading: Double = 0.0

        @JvmField
        var angleOfAttack: Double = 45.0

        @JvmField
        var isPreloaded: Boolean = true

        @JvmField
        var numSamples: Int = 2
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
            controller.addAction { robot.depositSample(Lift.upperBasketHeight) }
        }
        repeat(params.numSamples) {
            // Collect samples
            controller.addAction { goToSample() }
            controller.addAction { collectSample() }
            controller.addAction { goToBasket() }
            controller.addAction { robot.depositSample(Lift.upperBasketHeight) }
        }
        // Park
        controller.addAction { goToObservationZone() }
    }

    private fun goToSample(): Action {
        return robot.drive.actionBuilder(robot.drive.pose)
            .turnTo(Math.toRadians(-90.0))
            .waitSeconds(1.0)
            .strafeTo(Vector2d(FieldParams.samplePositionsX[currentSampleIndex], FieldParams.samplePositionsY[currentSampleIndex] + 26.0))
            .build()
    }

    private fun collectSample(): Action {
        return SequentialAction(
            robot.collectSample(),
            InstantAction { currentSampleIndex++ }
        )
    }

    private fun goToBasket(): Action {
        return robot.drive.actionBuilder(robot.drive.pose)
            .strafeTo(Vector2d(FieldParams.basketX, FieldParams.basketY))
            .turnTo(Math.toRadians(params.angleOfAttack))
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