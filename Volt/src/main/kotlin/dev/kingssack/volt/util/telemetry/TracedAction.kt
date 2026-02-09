package dev.kingssack.volt.util.telemetry

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action

class TracedAction(
    val label: String,
    private val inner: Action,
    private val trace: ActionTracer = ActionTracer,
) : Action {
    private var startTime: Long? = null

    val elapsedMs
        get() = startTime?.let { System.currentTimeMillis() - it } ?: 0L

    private var initialized = false

    override fun run(p: TelemetryPacket): Boolean {
        if (!initialized) {
            startTime = System.currentTimeMillis()
            trace.markRunning(this)
            initialized = true
        }

        val running = inner.run(p)
        if (!running) trace.markCompleted(this)
        return running
    }
}