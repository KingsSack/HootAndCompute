package org.firstinspires.ftc.teamcode.autonomous

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.SequentialAction
import com.acmerobotics.roadrunner.ftc.runBlocking
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.robot.Steve
import org.firstinspires.ftc.teamcode.util.MecanumDrive

class Auto(
    hardwareMap: HardwareMap,
    telemetry: Telemetry,
    initialPose: Pose2d,
    isPreloaded: Boolean,
    private val samplePoses: List<Pose2d>,
    basketPose: Pose2d,
    observationZonePose: Pose2d
) : Autonomous {
    // Robot
    private val robot = Steve(hardwareMap)

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

    private fun changeState(newState: State) {
        currentState = newState
        emitEvent(Event.ChangeState)
    }

    // Events
    private sealed class Event {
        data object ChangeState : Event()
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

    init {
        // Register drive
        registerDrive(hardwareMap, initialPose)

        // Display telemetry
        telemetry.addData("Current state", currentState.toString())
        telemetry.addData("Current sample", currentSampleIndex + 1)
        telemetry.addData("", "")
        telemetry.addData("Preloaded", isPreloaded)
        telemetry.addData("Basket position", basketPose)
        telemetry.addData("Observation zone position", observationZonePose)

        // Determine first state
        currentState = if (isPreloaded) State.GoingToBasket
        else if (samplePoses.isNotEmpty()) State.GoingToSample
        else State.GoingToObservationZone

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

    fun test() {
        // robot.control.addAction(goToObservationZone)
        runBlocking(goToObservationZone)
    }

    private fun handleEvent(event: Event) {
        when (currentState) {
            is State.Idle -> { }

            is State.GoingToSample -> when (event) {
                is Event.SampleReached -> {
                    changeState(State.CollectingSample)
                }
                else -> {
                    robot.control.addAction(goToSample)
                }
            }

            is State.CollectingSample -> when (event) {
                is Event.SampleCollected -> {
                    currentSampleIndex++
                    changeState(State.GoingToBasket)
                }
                else -> {
                    robot.control.addAction(collectSample)
                }
            }

            is State.GoingToBasket -> when (event) {
                is Event.BasketReached -> {
                    changeState(State.DepositingSample)
                }
                else -> {
                    robot.control.addAction(goToBasket)
                }
            }

            is State.DepositingSample -> when (event) {
                is Event.SampleDeposited -> {
                    val newState = if (currentSampleIndex < samplePoses.size) State.GoingToSample else State.GoingToObservationZone
                    changeState(newState)
                }
                else -> {
                    robot.control.addAction(depositSample)
                }
            }

            is State.GoingToObservationZone -> when (event) {
                is Event.ObservationZoneReached -> {
                    changeState(State.Park)
                }
                else -> {
                    robot.control.addAction(goToObservationZone)
                }
            }

            is State.Park -> { }
        }
    }

    private val goToSample: Action = if (samplePoses.isNotEmpty()) SequentialAction(
        drive.actionBuilder(drive.pose)
            .splineTo(samplePoses[currentSampleIndex].position, samplePoses[currentSampleIndex].heading)
            .build(),
        emitEvent(Event.SampleReached)
    ) else Action { true }

    private val collectSample: Action = SequentialAction(
        robot.claw.open(),
        robot.extender.extend(),
        robot.claw.close(),
        robot.extender.retract(),
        emitEvent(Event.SampleCollected)
    )

    private val goToBasket: Action = SequentialAction(
        drive.actionBuilder(drive.pose)
            .splineTo(basketPose.position, basketPose.heading)
            .build(),
        emitEvent(Event.BasketReached)
    )

    private val depositSample: Action = SequentialAction(
        robot.lift.raise(),
        robot.extender.extend(),
        robot.claw.open(),
        robot.extender.retract(),
        robot.claw.close(),
        robot.lift.drop(),
        emitEvent(Event.SampleDeposited)
    )

    private val goToObservationZone: Action = SequentialAction(
        drive.actionBuilder(drive.pose)
            .strafeTo(observationZonePose.position)
            // .splineTo(observationZonePose.position, observationZonePose.heading)
            .build(),
        emitEvent(Event.ObservationZoneReached)
    )
}