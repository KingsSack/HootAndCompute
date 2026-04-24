package dev.kingssack.volt.ai

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import dev.kingssack.volt.opmode.VoltOpMode
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.util.EventHandler
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * A [VoltOpMode] for controlling a [robot] with a Large Language Model.
 *
 * Provides a [WebSocket server][AIServer] interface for AI clients to execute robot actions
 * remotely. Actions are queued and executed sequentially, with state updates broadcast to connected
 * clients.
 *
 * @param R the robot type
 * @param serverPort the port for the [AIServer] (default: 8081)
 */
abstract class AIOpMode<R : Robot>(serverPort: Int = 8081) : VoltOpMode<R>() {
    @Suppress("unused")
    object Register : Registrar() {
        override fun register(
            registrationHelper: VoltRegistrationHelper,
            clazz: Class<out VoltOpMode<*>>,
        ) {
            if (clazz.isAnnotationPresent(VoltOpModeMeta::class.java)) {
                val annotation = clazz.getAnnotation(VoltOpModeMeta::class.java)
                if (annotation != null) {
                    registrationHelper.register(
                        clazz.getDeclaredConstructor(),
                        OpModeMeta.Builder()
                            .setName(annotation.name)
                            .setGroup(annotation.group)
                            .setFlavor(OpModeMeta.Flavor.TELEOP)
                            .setSource(OpModeMeta.Source.EXTERNAL_LIBRARY)
                            .build(),
                    )
                }
            }
        }
    }

    companion object {
        private const val PING_INTERVAL_MS = 5000L
        private const val MAX_PENDING_ACTIONS = 10
    }

    private var aiServer = AIServer(serverPort)
    private val pendingActions = ConcurrentLinkedQueue<PendingAction>()
    private var currentPendingAction: PendingAction? = null
    private var lastPingTime = 0L
    private val dashboard = FtcDashboard.getInstance()

    /**
     * Wrapper for an action with its metadata for tracking.
     *
     * @property action the action to execute
     * @property actionId the unique ID of the action
     * @property requestId the client's unique request ID
     */
    private data class PendingAction(
        val action: Action,
        val actionId: String,
        val requestId: String?,
    )

    init {
        telemetry.addData("Status", "Initializing Agent...")
        telemetry.update()
    }

    override fun begin() {
        ActionRegistry.clear()
        ActionRegistry.registerInstance(robot)
        for (attachment in robot.attachments) {
            ActionRegistry.registerInstance(attachment)
        }

        try {
            aiServer.start()
            aiServer.setActionCallback { actionId, params, requestId ->
                executeAction(actionId, params, requestId)
            }
            aiServer.setStateProvider { buildRobotState() }

            while (opModeIsActive()) {
                telemetry.addData("Status", "Agent is running")
                telemetry.addData(
                    "Connection",
                    if (aiServer.hasActiveConnection()) "Active" else "None",
                )
                telemetry.addData("Pending Actions", pendingActions.size)
                telemetry.addData("Current Action", currentPendingAction?.actionId ?: "None")
                tick()
                telemetry.update()
            }
        } catch (e: Exception) {
            telemetry.addData("Status", "AI mode crashed: ${e.message}")
            telemetry.update()
        } finally {
            aiServer.stop()
            ActionRegistry.clear()
        }
    }

    private fun executeAction(
        actionId: String,
        params: Map<String, Any?>,
        requestId: String?,
    ): AIServer.ActionResult {
        // Check queue capacity
        if (pendingActions.size >= MAX_PENDING_ACTIONS) {
            return AIServer.ActionResult(
                success = false,
                message = "Action queue full (max $MAX_PENDING_ACTIONS)",
                actionId = actionId,
            )
        }

        return try {
            val action = ActionRegistry.execute(actionId, params)
            if (action != null) {
                pendingActions.add(PendingAction(action, actionId, requestId))
                AIServer.ActionResult(
                    success = true,
                    message = "Action queued: $actionId",
                    actionId = actionId,
                )
            } else {
                AIServer.ActionResult(
                    success = false,
                    message = "Unknown action: $actionId",
                    actionId = actionId,
                )
            }
        } catch (e: ActionRegistry.ParameterValidationException) {
            AIServer.ActionResult(
                success = false,
                message = "Validation error: ${e.message}",
                actionId = actionId,
            )
        } catch (e: Exception) {
            AIServer.ActionResult(
                success = false,
                message = "Execution error: ${e.message}",
                actionId = actionId,
            )
        }
    }

    /** Main tick function called each loop iteration. */
    override fun tick() {
        val packet = TelemetryPacket()

        // Process current action
        currentPendingAction?.let { pending ->
            try {
                if (!pending.action.run(packet)) {
                    // Action completed successfully
                    aiServer.broadcastActionComplete(
                        actionId = pending.actionId,
                        requestId = pending.requestId,
                        success = true,
                        message = "Action completed: ${pending.actionId}",
                    )
                    currentPendingAction = null
                }
            } catch (e: Exception) {
                // Action failed
                aiServer.broadcastActionComplete(
                    actionId = pending.actionId,
                    requestId = pending.requestId,
                    success = false,
                    message = "Action failed: ${e.message}",
                )
                currentPendingAction = null
            }
        }

        // Start next action if idle
        if (currentPendingAction == null) {
            currentPendingAction = pendingActions.poll()
        }

        // Send telemetry packet to dashboard
        dashboard.sendTelemetryPacket(packet)

        // Periodic ping for connection health
        val now = System.currentTimeMillis()
        if (now - lastPingTime > PING_INTERVAL_MS) {
            aiServer.sendPing()
            lastPingTime = now
        }

        broadcastRobotState()
    }

    /** Broadcasts the current robot state to connected AI clients. */
    protected open fun broadcastRobotState() {
        val state = buildRobotState()
        aiServer.broadcastState(state)
    }

    /**
     * Builds the current robot state map to broadcast to clients.
     *
     * Override this method to include additional state information.
     *
     * @return a map of state key-value pairs
     */
    protected open fun buildRobotState(): Map<String, Any> =
        mapOf(
            "hasActiveAction" to (currentPendingAction != null),
            "currentActionId" to (currentPendingAction?.actionId ?: ""),
            "pendingActions" to pendingActions.size,
            "connectionAlive" to aiServer.hasActiveConnection(),
        )
}
