package dev.kingssack.volt.util

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import dev.kingssack.volt.core.VoltActionBuilder
import dev.kingssack.volt.util.telemetry.ActionTracer

class EventHandler {
    private data class Binding<P>(val configurator: VoltActionBuilder.(P) -> Unit)

    private val bindings = mutableMapOf<Event<*>, MutableList<Binding<*>>>()

    fun <P> bind(event: Event<P>, block: VoltActionBuilder.(P) -> Unit) {
        bindings.getOrPut(event) { mutableListOf() }.add(Binding(block))
    }

    private val runningActions = mutableListOf<Action>()

    private val dash: FtcDashboard? = FtcDashboard.getInstance()

    private var started = false

    private fun processEvents() {
        bindings.forEach { (event, eventBindings) ->
            val triggered =
                when (event) {
                    is Event.AutonomousEvent.Start -> !started
                    else -> event.shouldTrigger()
                }

            if (triggered) {
                eventBindings.forEach { binding ->
                    @Suppress("UNCHECKED_CAST") val typedBinding = binding as Binding<Any?>
                    val builder = VoltActionBuilder()
                    runningActions.add(
                        builder
                            .apply { typedBinding.configurator(builder, event.parameter) }
                            .build()
                    )
                }
            }
        }
        started = true
    }

    private fun runActions() {
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

    internal operator fun invoke() {
        processEvents()
        runActions()
    }
}
