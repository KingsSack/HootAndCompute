package dev.kingssack.volt.ai

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.opmode.VoltOpMode
import dev.kingssack.volt.robot.Robot
import java.util.concurrent.ConcurrentLinkedQueue

abstract class AIOpMode<R: Robot>(
    robotFactory: (HardwareMap) -> R,
    private val serverPort: Int = 8081
) : VoltOpMode<R>(robotFactory) {
    private lateinit var aiServer: AIServer
    private val pendingActions = ConcurrentLinkedQueue<Action>()
    private var currentAction: Action? = null

    override fun initialize() {
        telemetry.addData("Status", "Initializing Agent...")
        telemetry.update()

        ActionRegistry.clear()
        ActionRegistry.registerInstance(robot)

        aiServer = AIServer(serverPort)
    }

    override fun begin() {
        aiServer.setActionCallback { actionId, params -> executeAction(actionId, params) }
        while (opModeIsActive()) {
            telemetry.addData("Status", "Agent is running")
            tick()
            telemetry.update()
        }
        aiServer.stop()
        ActionRegistry.clear()
    }

    private fun executeAction(actionId: String, params: Map<String, Any?>): AIServer.ActionResult {
        return try {
            val action = ActionRegistry.execute(actionId, params)
            if (action != null) {
                pendingActions.add(action)
                AIServer.ActionResult(true, "Action queued: $actionId")
            } else {
                AIServer.ActionResult(false, "Unknown action: $actionId")
            }
        } catch (e: Exception) {
            AIServer.ActionResult(false, "Execution error: ${e.message}")
        }
    }

    fun tick() {
        currentAction?.let { action ->
            val p = TelemetryPacket()
            if (!action.run(p)) {
                currentAction = null
            }
        }

        if (currentAction == null) {
            currentAction = pendingActions.poll()
        }

        broadcastRobotState()
    }

    protected open fun broadcastRobotState() {
        val state = buildRobotState()
        aiServer.broadcastState(state)
    }

    protected open fun buildRobotState(): Map<String, Any> {
        return mapOf(
            "hasActiveAction" to (currentAction != null),
            "pendingActions" to pendingActions.size,
        )
    }
}
