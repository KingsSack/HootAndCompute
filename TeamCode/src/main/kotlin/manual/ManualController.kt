package org.firstinspires.ftc.teamcode.manual

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * ManualController is a class that manages the manual control of the robot.
 *
 * @param telemetry for logging
 */
class ManualController(
    private val telemetry: Telemetry
) {
    private val dash: FtcDashboard? = FtcDashboard.getInstance()
    private var runningActions: MutableList<Action> = ArrayList()

    /**
     * Adds an action to the list of running actions.
     *
     * @param action the action to add
     */
    fun addAction(action: Action) {
        runningActions.add(action)
    }

    /**
     * Runs the actions in the list of running actions.
     */
    fun runActions() {
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
}