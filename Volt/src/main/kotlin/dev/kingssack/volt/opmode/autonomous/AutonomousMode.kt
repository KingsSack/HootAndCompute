package dev.kingssack.volt.opmode.autonomous

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.canvas.Canvas
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.frozenmilk.sinister.sdk.opmodes.OpModeScanner.RegistrationHelper
import dev.kingssack.volt.core.VoltActionBuilder
import dev.kingssack.volt.opmode.VoltOpMode
import dev.kingssack.volt.robot.Robot
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

/**
 * AutonomousMode is an abstract class that defines the methods for running an autonomous mode.
 *
 * @param R the type of robot
 * @property robot the robot instance
 */
abstract class AutonomousMode<R : Robot>(robotFactory: (HardwareMap) -> R) :
    VoltOpMode<R>(robotFactory) {
    private val dash: FtcDashboard? = FtcDashboard.getInstance()
    private val canvas = Canvas()
    abstract val name: String
    open val group: String = OpModeMeta.DefaultGroup
    open val autoTransition: String? = null
    override fun register(registrationHelper: RegistrationHelper) {
        registrationHelper.register(OpModeMeta.Builder().setName(name).setGroup(group).setFlavor(OpModeMeta.Flavor.AUTONOMOUS).setTransitionTarget(autoTransition).setSource(OpModeMeta.Source.EXTERNAL_LIBRARY).build(), javaClass.getDeclaredConstructor().newInstance())
    }
    override fun begin() {
        sequence()
    }

    /** Define the autonomous sequence using DSL. */
    protected abstract fun sequence()

    /** Execute the autonomous sequence. */
    protected fun execute(block: VoltActionBuilder<R>.() -> Unit) {
        val action = VoltActionBuilder(robot).apply(block).build()
        runAction(action)

        with(telemetry) {
            addData("Status", "Autonomous Complete")
            update()
        }
    }

    private fun runAction(action: Action) {
        action.preview(canvas)

        var running = true
        while (running && opModeIsActive() && !Thread.currentThread().isInterrupted) {
            val p = TelemetryPacket()
            p.fieldOverlay().operations.addAll(canvas.operations)

            running = action.run(p)

            context(telemetry) { robot.update() }
            dash?.sendTelemetryPacket(p)
        }
    }
}
