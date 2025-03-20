package dev.kingssack.volt.opmode.autonomous

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.canvas.Canvas
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import dev.kingssack.volt.robot.Robot

/**
 * AutonomousMode is an abstract class that defines the methods for running an autonomous mode.
 *
 * @param telemetry for logging
 */
abstract class AutonomousMode() {
    // Robot
    abstract val robot: Robot

    private val dash: FtcDashboard? = FtcDashboard.getInstance()
    private val canvas = Canvas()

    protected val actionSequence = mutableListOf<() -> Action>()

    /**
     * Execute the autonomous sequence.
     */
    fun execute() {
        for (action in actionSequence) {
            runAction(action())
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