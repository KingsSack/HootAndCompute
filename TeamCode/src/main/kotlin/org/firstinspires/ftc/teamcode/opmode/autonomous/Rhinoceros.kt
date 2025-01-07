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
 * Rhinoceros is an autonomous mode that places specimens on the top bar of the submersible.
 *
 * @param hardwareMap the hardware map
 * @param telemetry the telemetry
 * @param params the parameters for Rhinoceros
 *
 * @property controller the autonomous controller
 * @property robot the robot
 *
 * @see AutonomousMode
 */
class Rhinoceros(
    hardwareMap: HardwareMap,
    telemetry: Telemetry,
    params: RhinocerosParams
) : AutonomousMode {
    /**
     * The parameters for Rhinoceros.
     *
     * @property initialX the initial x position
     * @property initialY the initial y position
     * @property initialHeading the initial heading
     * @property numSamples the number of samples to convert
     */
    class RhinocerosParams {
        @JvmField
        var initialX: Double = -24.0
        @JvmField
        var initialY: Double = 66.0
        @JvmField
        var initialHeading: Double = -90.0

        @JvmField
        var numSamples: Int = 3
    }

    override val controller = AutonomousController(telemetry)
    override val robot = Steve(hardwareMap, Pose2d(
        Vector2d(params.initialX, params.initialY),
        Math.toRadians(params.initialHeading)
    ))

    private var currentSampleIndex = 0
    private var currentSubmersiblePosition = Vector2d(FieldParams.submersibleX, FieldParams.submersibleY + 20)

    init {
        // Autonomous sequence
        controller.addAction { goToSubmersible() }
        controller.addAction { depositSpecimen() } // Deposit the preloaded specimen
        repeat(params.numSamples) {
            // Collect samples
            controller.addAction { goToSample() }
            controller.addAction { goToObservationZone() }
            controller.addAction { retrieveSpecimen() }
            controller.addAction { goToSubmersible() }
            controller.addAction { depositSpecimen() }
        }
        // Park
        controller.addAction { goToObservationZone() }
    }

    private fun goToSample(): Action {
        return SequentialAction(
            robot.drive.actionBuilder(robot.drive.pose)
                .strafeTo(Vector2d(-36.0, 42.0))
                .splineToLinearHeading(Pose2d(Vector2d(-FieldParams.samplePositionsX[currentSampleIndex], FieldParams.samplePositionsY[currentSampleIndex] - 16.0), Math.toRadians(90.0)), 0.0)
                .build(),
            InstantAction { currentSampleIndex++ }
        )
    }

    private fun goToSubmersible(): Action {
        return robot.drive.actionBuilder(robot.drive.pose)
            .strafeTo(currentSubmersiblePosition)
            .build()
    }

    private fun depositSpecimen(): Action {
        return SequentialAction(
            robot.lift.goTo(Lift.upperSubmersibleBarHeight),
            robot.extendArmToSubmersible(),
            robot.lift.drop(),
            InstantAction { currentSubmersiblePosition = Vector2d(currentSubmersiblePosition.x + 5.0, currentSubmersiblePosition.y) },
        )
    }

    private fun goToObservationZone(): Action {
        return robot.drive.actionBuilder(robot.drive.pose)
            .strafeTo(Vector2d(FieldParams.observationX, FieldParams.observationY))
            .build()
    }

    private fun retrieveSpecimen(): Action {
        return SequentialAction(
            robot.drive.actionBuilder(robot.drive.pose)
                .strafeTo(Vector2d(FieldParams.observationX, 45.0))
                .build(),
            robot.extendArm(),
            robot.claw.close(),
            robot.retractArm()
        )
    }

    override fun run() {
        controller.execute()
    }
}