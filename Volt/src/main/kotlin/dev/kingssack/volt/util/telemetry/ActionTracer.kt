package dev.kingssack.volt.util.telemetry

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import dev.kingssack.volt.util.telemetry.TracedAction
import org.firstinspires.ftc.robotcore.external.Telemetry
import java.util.concurrent.ConcurrentHashMap

object ActionTracer {
    private val trace = mutableListOf<TracedAction>()
    private val running = ConcurrentHashMap.newKeySet<TracedAction>()

    fun markRunning(action: TracedAction) {
        trace.add(action)
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
                running.forEachIndexed { i, action ->
                    addData("[$i]", "${action.label} (${action.elapsedMs}ms")
                }
            }
        }
    }

    context(packet: TelemetryPacket)
    fun writePacket() {
        trace.forEach { packet.put("action/${it.label}", it.elapsedMs) }
    }
}