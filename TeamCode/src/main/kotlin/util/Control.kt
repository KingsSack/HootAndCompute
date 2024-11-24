package util

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action

class Controller {
    private val packet = TelemetryPacket()
    private val dash: FtcDashboard? = FtcDashboard.getInstance()
    private var runningActions: MutableList<Action> = ArrayList()

    fun addAction(action: Action) {
        runningActions.add(action)
    }

    fun run() {
        val newActions: MutableList<Action> = ArrayList()
        for (action in runningActions) {
            action.preview(packet.fieldOverlay())
            if (action.run(packet)) {
                newActions.add(action)
            }
        }
        runningActions = newActions
        dash?.sendTelemetryPacket(packet)
    }
}