package dev.kingssack.volt.ai

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import dev.kingssack.volt.opmode.VoltOpMode
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.robot.RobotState
import dev.kingssack.volt.util.telemetry.ActionTracer
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta.Builder
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta.Flavor

/**
 * A [VoltOpMode] for controlling a [robot] with a Large Language Model.
 *
 * Provides a REST API server interface for AI clients to execute robot actions remotely. Actions
 * are submitted via the REST API, processed each tick, and results are returned to the client.
 * Supports parallel execution via a cached thread pool.
 *
 * @param R the robot type
 * @param serverPort the port for the [AIServer] (default: 8081)
 * @property server the [AIServer] instance
 */
abstract class AIOpMode<R : Robot>(private val serverPort: Int = 8081) : VoltOpMode<R>() {
    @Suppress("unused")
    object Register : Registrar() {
        override fun register(
            registrationHelper: VoltRegistrationHelper,
            clazz: Class<VoltOpMode<*>>,
        ) {
            if (clazz.isAnnotationPresent(VoltOpModeMeta::class.java)) {
                val annotation = clazz.getAnnotation(VoltOpModeMeta::class.java)
                if (annotation != null) {
                    registrationHelper.register(
                        clazz.getDeclaredConstructor(),
                        Builder()
                            .setName(annotation.name)
                            .setGroup(annotation.group)
                            .setFlavor(Flavor.TELEOP)
                            .setSource(OpModeMeta.Source.EXTERNAL_LIBRARY)
                            .build(),
                    )
                }
            }
        }
    }

    private val dash: FtcDashboard? = FtcDashboard.getInstance()

    private lateinit var registry: ActionRegistry

    private lateinit var server: AIServer

    data class RunningAction(val action: Action, val requestId: String)

    private val runningActions = mutableListOf<RunningAction>()

    init {
        telemetry.addData("Status", "Initializing Agent...")
        telemetry.update()
    }

    override fun begin() {
        registry = ActionRegistry(listOf(robot) + robot.attachments)
        server = AIServer(registry, serverPort)

        // Wire up the server callbacks
        server.stateProvider = { getRobotState() }

        telemetry.addData("Status", "Agent Ready")
        telemetry.update()

        super.begin()
    }

    override fun tick() {
        runningActions.addAll(server.processPendingExecutions())
        runActions()
        context(telemetry) { robot.update() }
    }

    override fun end() {
        super.end()
        server.shutdown()
    }

    private fun getRobotState(): String =
        when (val state = robot.state.value) {
            is RobotState.Initializing -> "The robot is initializing"
            is RobotState.Idle -> "The robot is idle"
            is RobotState.Running -> "The robot is running actions"
            is RobotState.Fault -> "The robot has encountered an error: ${state.error.message}"
        }

    private fun runActions() {
        val packet = TelemetryPacket()

        // Run actions and remove finished ones
        runningActions.removeAll { (action, requestId) ->
            action.preview(packet.fieldOverlay())
            if (!action.run(packet)) {
                server.resolveAction(requestId, true, "Action completed")
                return@removeAll true
            }
            false
        }

        // Write telemetry
        context(packet) { ActionTracer.writePacket() }
        dash?.sendTelemetryPacket(packet)
    }
}
