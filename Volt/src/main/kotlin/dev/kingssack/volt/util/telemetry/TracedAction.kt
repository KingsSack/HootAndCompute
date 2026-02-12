package dev.kingssack.volt.util.telemetry

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action

/**
 * TracedAction is a wrapper around an Action that tracks its execution time and reports it to an ActionTracer.
 *
 * @param label A human-readable label for the action, used in telemetry
 * @param inner The actual Action being wrapped and executed
 * @param trace The ActionTracer instance that will receive updates about this action's execution
 */
class TracedAction(
    val label: String,
    private val inner: Action,
    private val trace: ActionTracer = ActionTracer,
) : Action {
    var startTime: Long? = null
        private set

    var endTime: Long? = null
        private set

    val elapsedMs: Long
        get() =
            when {
                startTime == null -> 0L
                endTime != null -> (endTime!! - startTime!!) / 1_000_000
                else -> (System.nanoTime() - startTime!!) / 1_000_000
            }

    private var initialized = false

    override fun run(p: TelemetryPacket): Boolean {
        if (!initialized) {
            startTime = System.nanoTime()
            trace.markRunning(this)
            initialized = true
        }

        val running = inner.run(p)
        if (!running) {
            endTime = System.nanoTime()
            trace.markCompleted(this)
        }
        return running
    }
}
