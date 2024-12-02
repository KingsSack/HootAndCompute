package org.firstinspires.ftc.teamcode.autonomous

import com.acmerobotics.roadrunner.*
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.Configuration.OtterParams
import org.firstinspires.ftc.teamcode.Configuration.fieldParams
import org.firstinspires.ftc.teamcode.robot.Steve
import org.firstinspires.ftc.teamcode.util.MecanumDrive

class Otter(
    hardwareMap: HardwareMap,
    telemetry: Telemetry,
    params: OtterParams
) : AutonomousMode {
    override val controller = AutonomousController(telemetry)
    override val robot = Steve(hardwareMap)
    override val drive = MecanumDrive(
        hardwareMap, 
        Pose2d(Vector2d(params.initialX, params.initialY), Math.toRadians(params.initialHeading))
    )

    private var currentSampleIndex = 0

    init {
        // Build the sequence of actions
        if (params.isPreloaded) {
            controller.addAction(goToBasket())
            controller.addAction(robot.depositSample(fieldParams.lowerBasketHeight))
        } else {
            repeat(params.numSamples) {
                controller.addAction(goToSample())
                controller.addAction(collectSample())
                controller.addAction(goToBasket())
                controller.addAction(robot.depositSample(fieldParams.lowerBasketHeight))
            }
        }
        controller.addAction(goToObservationZone())
    }

    private fun goToSample(): Action {
        return drive.actionBuilder(drive.pose)
            .splineTo(Vector2d(fieldParams.samplePositionsX[currentSampleIndex], fieldParams.samplePositionsY[currentSampleIndex]), Math.toRadians(-90.0))
            .build()
    }

    private fun collectSample(): Action {
        return SequentialAction(
            robot.collectSample(),
            InstantAction { currentSampleIndex++ }
        )
    }

    private fun goToBasket(): Action {
        return drive.actionBuilder(drive.pose)
            .splineTo(Vector2d(fieldParams.basketX, fieldParams.basketY), Math.toRadians(45.0))
            .build()
    }

    private fun goToObservationZone(): Action {
        return drive.actionBuilder(drive.pose)
            .strafeTo(Vector2d(fieldParams.observationX, fieldParams.observationY))
            .build()
    }

    override fun run() {
        controller.execute()
    }
}