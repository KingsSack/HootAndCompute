package dev.kingssack.volt.autonomous

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.canvas.Canvas
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import dev.kingssack.volt.robot.Robot

/**
 * AutonomousMode is an abstract class that defines the methods for running an autonomous mode.
 */
abstract class AutonomousMode : LinearOpMode() {
    abstract val robot: Robot

    private val dash: FtcDashboard? = FtcDashboard.getInstance()
    private val canvas = Canvas()

    protected val actionSequence = mutableListOf<() -> Action>()

    override fun runOpMode() {
        waitForStart()
        execute()
    }

    /**
     * Execute the autonomous sequence.
     */
    fun execute() {
        for (action in actionSequence) {
            runAction(action())
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

            robot.update(telemetry)
            dash?.sendTelemetryPacket(p)
        }
    }
}