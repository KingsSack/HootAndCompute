package dev.kingssack.volt.manual

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import dev.kingssack.volt.robot.Robot
import org.firstinspires.ftc.robotcore.external.Telemetry
import kotlin.math.abs
import kotlin.math.pow

/**
 * ManualMode is an abstract class that defines the methods for running a manual mode.
 *
 * @param telemetry for logging
 */
abstract class ManualMode(private val telemetry: Telemetry, private val params: ManualParams = ManualParams()) {
    /**
     * Configuration object for manual control.
     *
     * @property deadzone the minimum joystick input to register
     * @property inputExp the input exponential for fine control
     */
    class ManualParams(
        val deadzone: Double = 0.1,
        val inputExp: Double = 2.0
    )

    // Robot
    abstract val robot: Robot

    private val dash: FtcDashboard? = FtcDashboard.getInstance()

    protected var runningActions = mutableListOf<Action>()

    /**
     * Runs the actions in the list of running actions.
     */
    protected fun runActions() {
        val packet = TelemetryPacket()
        val newActions: MutableList<Action> = ArrayList()
        for (action in runningActions) {
            action.preview(packet.fieldOverlay())
            if (action.run(packet)) {
                newActions.add(action)
            }
        }
        runningActions = newActions
        dash?.sendTelemetryPacket(packet)
        telemetry.update()
    }

    /**
     * Processes an input with deadzone and exponential scaling.
     *
     * @param input the input to process
     * @return the processed input
     */
    protected fun processInput(input: Double): Double {
        // Apply deadzone
        if (abs(input) < params.deadzone) return 0.0

        // Normalize input
        val normalizedInput = (input - params.deadzone) / (1 - params.deadzone)

        // Apply exponential scaling for fine control
        return normalizedInput.pow(params.inputExp) * if (input < 0) -1 else 1
    }

    /**
     * Tick the manual mode.
     *
     * @param telemetry for logging
     */
    abstract fun tick(telemetry: Telemetry)
}