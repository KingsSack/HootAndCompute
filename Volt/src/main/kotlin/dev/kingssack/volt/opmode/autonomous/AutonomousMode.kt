package dev.kingssack.volt.opmode.autonomous

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.canvas.Canvas
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.util.ActionSequenceBuilder

/**
 * AutonomousMode is an abstract class that defines the methods for running an autonomous mode.
 *
 * @property robot the robot instance
 */
abstract class AutonomousMode<R : Robot>(private val robotFactory: (HardwareMap) -> R) :
    LinearOpMode() {
    protected lateinit var robot: R
        private set

    private val dash: FtcDashboard? = FtcDashboard.getInstance()
    private val canvas = Canvas()

    override fun runOpMode() {
        robot = robotFactory(hardwareMap)
        waitForStart()
        sequence()
    }

    /** Define the autonomous sequence using DSL. */
    protected abstract fun sequence()

    /** Execute the autonomous sequence. */
    protected fun execute(block: ActionSequenceBuilder.() -> Unit) {
        val builder = ActionSequenceBuilder().apply(block)
        runAction(builder.build())
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
