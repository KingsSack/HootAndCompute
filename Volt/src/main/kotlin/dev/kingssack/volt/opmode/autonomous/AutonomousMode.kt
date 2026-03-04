package dev.kingssack.volt.opmode.autonomous

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.canvas.Canvas
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.core.VoltActionBuilder
import dev.kingssack.volt.opmode.VoltOpMode
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.util.Event
import dev.kingssack.volt.util.telemetry.ActionTracer

/**
 * AutonomousMode is an abstract class that defines the methods for running an autonomous mode.
 *
 * @param R the type of robot
 * @property robot the robot instance
 */
abstract class AutonomousMode<R : Robot>(robotFactory: (HardwareMap) -> R) :
    VoltOpMode<R>(robotFactory) {
    private val dash: FtcDashboard? = FtcDashboard.getInstance()
    private val canvas = Canvas()

    private val events =
        mutableListOf<Pair<Event.AutonomousEvent, VoltActionBuilder<R>.() -> Unit>>()

    protected fun onStart(block: VoltActionBuilder<R>.() -> Unit) {
        events.add(Event.AutonomousEvent.Start to block)
    }

    /** Define actions to be triggered by events */
    abstract fun defineEvents()

    /** Defines autonomous events */
    override fun initialize() {
        super.initialize()
        defineEvents()
    }

    override fun begin() {
        events.forEach { (event, action) ->
            when (event) {
                Event.AutonomousEvent.Start ->
                    runAction(VoltActionBuilder(robot).apply(action).build())
            }
        }
    }

    private fun runAction(action: Action) {
        action.preview(canvas)

        var running = true
        while (running && opModeIsActive() && !Thread.currentThread().isInterrupted) {
            val packet = TelemetryPacket()
            packet.fieldOverlay().operations.addAll(canvas.operations)

            running = action.run(packet)

            context(telemetry) { robot.update() }
            context(packet) { ActionTracer.writePacket() }
            dash?.sendTelemetryPacket(packet)
        }
    }
}
