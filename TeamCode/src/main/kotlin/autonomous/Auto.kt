package autonomous

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.SequentialAction
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
import robot.Steve
import util.MecanumDrive

class Auto(
    private val robot: Steve,
    private val isPreloaded: Boolean,
    private val samplePoses: List<Pose2d>,
    private val basketPose: Pose2d,
    private val observationZonePose: Pose2d
) : Autonomous {
    // States
    private sealed class State {
        data object Idle : State()
        data object GoingToSample : State()
        data object CollectingSample : State()
        data object GoingToBasket : State()
        data object DepositingSample : State()
        data object GoingToObservationZone : State()
        data object Park : State()
    }
    private var currentState: State = State.Idle

    // Events
    private sealed class Event {
        data object Begin : Event()
        data object SampleReached : Event()
        data object SampleCollected : Event()
        data object BasketReached : Event()
        data object SampleDeposited : Event()
        data object ObservationZoneReached : Event()
    }
    private class EmitEvent(private val auto: Auto, private val event: Event) : Action {
        override fun run(p: TelemetryPacket): Boolean {
            auto.handleEvent(event)
            return false
        }
    }
    private fun emitEvent(event: Event) : Action {
        return EmitEvent(this, event)
    }

    // Sample index
    private var currentSampleIndex = 0

    // Drive
    private lateinit var drive: MecanumDrive

    override fun init(hardwareMap: HardwareMap, telemetry: Telemetry, initialPose: Pose2d) {
        // Register drive
        registerDrive(hardwareMap, initialPose)

        // Display telemetry
        telemetry.addData("Current state", currentState.toString())
        telemetry.addData("Current sample", currentSampleIndex + 1)
        telemetry.addData("", "")
        telemetry.addData("Preloaded", isPreloaded)
        telemetry.addData("Basket position", basketPose)
        telemetry.addData("Observation zone position", observationZonePose)

        // Handle event
        handleEvent(Event.Begin)

        // Update telemetry
        telemetry.update()
    }

    override fun registerDrive(hardwareMap: HardwareMap, initialPose: Pose2d) {
        // Create new mecanum drive
        drive = MecanumDrive(hardwareMap, initialPose)
    }

    override fun tick(telemetry: Telemetry) {
        // Display telemetry
        telemetry.addData("Current state", currentState.toString())
        telemetry.addData("Current sample", currentSampleIndex + 1)

        // Tick robot
        robot.tick()

        // Update telemetry
        telemetry.update()
    }

    private fun handleEvent(event: Event) {
        when (currentState) {
            is State.Idle -> when (event) {
                is Event.Begin -> {
                    currentState = if (samplePoses.isNotEmpty())
                        if (isPreloaded) State.GoingToBasket
                        else State.GoingToSample
                    else State.GoingToObservationZone
                }
                else -> {
                    // Do nothing
                }
            }

            is State.GoingToSample -> when (event) {
                is Event.SampleReached -> {
                    currentState = State.CollectingSample
                }
                else -> {
                    robot.control.addAction(goToSample)
                }
            }

            is State.CollectingSample -> when (event) {
                is Event.SampleCollected -> {
                    currentSampleIndex++
                    currentState = State.GoingToBasket
                }
                else -> {
                    robot.control.addAction(collectSample)
                }
            }

            is State.GoingToBasket -> when (event) {
                is Event.BasketReached -> {
                    currentState = State.DepositingSample
                }
                else -> {
                    robot.control.addAction(goToBasket)
                }
            }

            is State.DepositingSample -> when (event) {
                is Event.SampleDeposited -> {
                    currentState = if (currentSampleIndex < samplePoses.size) State.GoingToSample else State.GoingToObservationZone
                }
                else -> {
                    robot.control.addAction(depositSample)
                }
            }

            is State.GoingToObservationZone -> when (event) {
                is Event.ObservationZoneReached -> {
                    currentState = State.Park
                }
                else -> {
                    robot.control.addAction(goToObservationZone)
                }
            }

            is State.Park -> {
                // Do nothing
            }
        }
    }

    private val goToSample = SequentialAction(
        drive.actionBuilder(drive.pose)
            .splineTo(samplePoses[currentSampleIndex].position, samplePoses[currentSampleIndex].heading)
            .build(),
        emitEvent(Event.SampleReached)
    )

    private val collectSample = SequentialAction(
        robot.claw.open(),
        robot.extender.extend(),
        robot.claw.close(),
        robot.extender.retract(),
        emitEvent(Event.SampleCollected)
    )

    private val goToBasket = SequentialAction(
        drive.actionBuilder(drive.pose)
            .splineTo(basketPose.position, basketPose.heading)
            .build(),
        emitEvent(Event.BasketReached)
    )

    private val depositSample = SequentialAction(
        robot.lift.liftUp(),
        robot.extender.extend(),
        robot.claw.open(),
        robot.extender.retract(),
        robot.claw.close(),
        emitEvent(Event.SampleDeposited)
    )

    private val goToObservationZone = SequentialAction(
        drive.actionBuilder(drive.pose)
            .splineTo(observationZonePose.position, observationZonePose.heading)
            .build(),
        emitEvent(Event.ObservationZoneReached)
    )
}