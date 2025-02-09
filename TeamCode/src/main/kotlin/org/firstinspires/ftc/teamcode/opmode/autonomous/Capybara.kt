package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.roadrunner.*
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.autonomous.AutonomousMode
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.attachment.Lift
import org.firstinspires.ftc.teamcode.robot.Steve
import org.firstinspires.ftc.teamcode.util.FieldParams

class Capybara(
    hardwareMap: HardwareMap,
    telemetry: Telemetry
) : AutonomousMode(telemetry) {
    override val robot = Steve(hardwareMap, Pose2d(
        Vector2d(-24.0, 63.4),
        Math.toRadians(-90.0)
    ))

    private var currentSubmersiblePosition = Vector2d(FieldParams.submersibleX, FieldParams.submersibleY + 14)

    init {
        actionSequence.add { initialize() }
        // Deposit the preloaded specimen
        actionSequence.add { goToSubmersible() }
        actionSequence.add { depositSpecimen() }
        // Collect second specimen
        actionSequence.add { retrieveSpecimen() }
        actionSequence.add { turnTowardsSubmersible() }
        actionSequence.add { goToSubmersible() }
        actionSequence.add { depositSpecimen() }
        // Park
        actionSequence.add { goToObservationZone() }
    }

    private fun initialize(): Action {
        return robot.tail.retract()
    }

    private fun goToSubmersible(): Action {
        return SequentialAction(robot.strafeTo(currentSubmersiblePosition))
    }

    private fun depositSpecimen(): Action {
        return SequentialAction(
            robot.depositSpecimen(Lift.upperSubmersibleBarHeight),
            InstantAction { currentSubmersiblePosition = Vector2d(currentSubmersiblePosition.x + 5.0, currentSubmersiblePosition.y) },
        )
    }

    private fun retrieveSpecimen(): Action {
        return SequentialAction(
            robot.strafeToLinearHeading(Vector2d(FieldParams.observationX - 5.0, 44.0), Math.toRadians(90.0)),
            robot.retrieveSpecimen(),
        )
    }

    private fun turnTowardsSubmersible(): Action {
        return robot.turnTo(Math.toRadians(-90.0))
    }

    private fun goToObservationZone(): Action {
        return robot.strafeTo(Vector2d(FieldParams.observationX - 5.0, FieldParams.observationY))
    }
}