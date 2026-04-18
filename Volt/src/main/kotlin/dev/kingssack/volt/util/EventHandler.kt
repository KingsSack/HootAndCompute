package dev.kingssack.volt.util

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import dev.kingssack.volt.core.VoltActionBuilder
import dev.kingssack.volt.util.buttons.AnalogHandler
import dev.kingssack.volt.util.buttons.AnalogInput
import dev.kingssack.volt.util.buttons.Button
import dev.kingssack.volt.util.buttons.ButtonHandler
import dev.kingssack.volt.util.telemetry.ActionTracer
import java.util.*

sealed class EventHandler<E : Event> {
    protected val bindings = mutableListOf<Pair<E, VoltActionBuilder.() -> Unit>>()

    /** Binds [block] to an [event]. */
    fun on(
        event: E,
        block: VoltActionBuilder.() -> Unit,
    ) {
        bindings.add(event to block)
    }

    abstract operator fun invoke()

    protected val runningActions = mutableListOf<Action>()

    private val dash: FtcDashboard? = FtcDashboard.getInstance()

    internal fun runActions() {
        val packet = TelemetryPacket()

        // Run actions and remove finished ones
        runningActions.removeAll { action ->
            action.preview(packet.fieldOverlay())
            !action.run(packet)
        }

        // Write telemetry
        context(packet) { ActionTracer.writePacket() }
        dash?.sendTelemetryPacket(packet)
    }

    /** Event handler for autonomous modes. */
    class AutonomousHandler : EventHandler<Event.AutonomousEvent>() {
        private var startDispatched = false

        override fun invoke() {
            bindings.forEach { (event, block) ->
                when (event) {
                    Event.AutonomousEvent.Start -> {
                        if (!startDispatched) {
                            startDispatched = true
                            runningActions.add(VoltActionBuilder().apply(block).build())
                        }
                    }

                    else -> {
                        if (event.trigger()) runningActions.add(VoltActionBuilder().apply(block).build())
                    }
                }
            }
        }
    }

    /**
     * Event handler for manual modes.
     *
     * @param buttonHandlers the button handlers to check for button events
     * @param analogHandlers the analog handlers to check for analog events
     */
    class ManualHandler(
        private val buttonHandlers: EnumMap<Button, ButtonHandler>,
        private val analogHandlers: EnumMap<AnalogInput, AnalogHandler>,
    ) : EventHandler<Event.ManualEvent>() {
        override fun invoke() {
            bindings.forEach { (event, block) ->
                if (event.trigger(buttonHandlers, analogHandlers)) {
                    runningActions.add(VoltActionBuilder().apply(block).build())
                }
            }
        }
    }
}
