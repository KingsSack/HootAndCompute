package org.firstinspires.ftc.teamcode.autonomous

import org.firstinspires.ftc.robotcore.external.Telemetry
import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.canvas.Canvas
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action

/**
 * AutonomousController is a class that manages the execution of autonomous actions.
 *
 * @param telemetry for logging
 */
class AutonomousController(
    private val telemetry: Telemetry
) {
    private val dash: FtcDashboard? = FtcDashboard.getInstance()
    private val canvas = Canvas()
    private val actions = mutableListOf<Action>()

    /**
     * Add an action to the autonomous sequence.
     *
     * @param action the action to add
     */
    fun addAction(action: Action) {
        actions.add(action)
    }

    /**
     * Execute the autonomous sequence.
     */
    fun execute() {
        for (action in actions) {
            runAction(action)
            telemetry.update()
        }
        telemetry.addData("Autonomous", "Completed")
        telemetry.update()
    }

    /**
     * Run an action.
     *
     * @param action the action to run
     */
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