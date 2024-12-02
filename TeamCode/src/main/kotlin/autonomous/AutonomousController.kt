package org.firstinspires.ftc.teamcode.autonomous

import org.firstinspires.ftc.robotcore.external.Telemetry
import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.canvas.Canvas
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action

class AutonomousController(
    private val telemetry: Telemetry
) {
    private val dash: FtcDashboard? = FtcDashboard.getInstance()
    private val canvas = Canvas()
    private val actions = mutableListOf<Action>()

    fun addAction(action: Action) {
        actions.add(action)
    }

    fun execute() {
        for (action in actions) {
            runAction(action)
            telemetry.update()
        }
        telemetry.addData("Autonomous", "Completed")
        telemetry.update()
    }

    private fun runAction(action: Action) {
        action.preview(canvas)

        var running = true
        while (running && !Thread.currentThread().isInterrupted) {
            val p = TelemetryPacket()
            p.fieldOverlay().operations.addAll(canvas.operations)

            running = action.run(p)

            dash?.sendTelemetryPacket(p)
        }
    }
}