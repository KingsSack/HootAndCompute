package dev.kingssack.volt.util.telemetry

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import java.util.concurrent.ConcurrentHashMap
import org.firstinspires.ftc.robotcore.external.Telemetry

object ActionTracer {
    private const val MAX_TRACE_SIZE = 100
    private val trace = mutableListOf<TracedAction>()
    private val running = ConcurrentHashMap.newKeySet<TracedAction>()

    fun markRunning(action: TracedAction) {
        trace.add(action)
        if (trace.size > MAX_TRACE_SIZE) {
            trace.removeAt(0)
        }

        running.add(action)
    }

    fun markCompleted(action: TracedAction) {
        running.remove(action)
    }

    context(telemetry: Telemetry)
    fun writeTelemetry() {
        with(telemetry) {
            addLine("=== Running Actions ===")
            addLine()

            if (running.isEmpty()) {
                addLine("None")
            } else {
                running
                    .toList()
                    .sortedBy { it.startTime }
                    .forEachIndexed { i, action ->
                        addData("[$i]", "${action.label} (${action.elapsedMs}ms)")
                    }
            }
        }
    }

    context(packet: TelemetryPacket)
    fun writePacket() {
        trace.forEachIndexed { i, action ->
            packet.put("action/$i/${action.label}", action.elapsedMs)
        }
    }
}
