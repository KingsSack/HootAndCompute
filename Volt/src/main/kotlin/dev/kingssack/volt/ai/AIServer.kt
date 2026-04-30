package dev.kingssack.volt.ai

import com.google.gson.Gson
import com.google.gson.JsonObject
import fi.iki.elonen.NanoHTTPD
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/**
 * A REST API for controlling a robot with an AI agent.
 *
 * Provides endpoints for listing available actions, retrieving robot state, and executing actions.
 * Actions are executed via a cached thread pool to support parallel execution.
 *
 * @param port the port to listen on (default: 8081)
 */
class AIServer(port: Int = 8081) : NanoHTTPD(port) {
    private val gson = Gson()

    private val pendingExecutions = ConcurrentHashMap<String, ActionPending>()
    private val executionPool = Executors.newCachedThreadPool { runnable ->
        val thread = Thread(runnable, "ai-action-executor")
        thread.isDaemon = true
        thread
    }

    /** Callback to retrieve the current robot state message. Set by [AIOpMode] each tick. */
    var stateProvider: (() -> String)? = null

    /**
     * Callback to execute an action and return it, or null if not found. Set by [AIOpMode] each
     * tick.
     */
    var executor:
            ((actionId: String, params: Map<String, Any?>) -> com.acmerobotics.roadrunner.Action?)? =
        null

    init {
        start(SOCKET_READ_TIMEOUT, false)
    }

    /**
     * Processes all pending action executions.
     *
     * Submits each pending execution to the thread pool. The result is stored back in the
     * [ActionPending] for clients to retrieve via the REST API.
     *
     * @return a list of actions that were successfully queued for execution
     */
    fun processPendingExecutions(): List<AIOpMode.RunningAction> {
        val snapshot = pendingExecutions.entries.toList()
        val newActions = mutableListOf<AIOpMode.RunningAction>()
        for ((requestId, pending) in snapshot) {
            if (pending.resultData != null) continue
            executionPool.submit {
                try {
                    val action = executor?.invoke(pending.actionId, pending.params)
                    if (action != null) {
                        pending.resultData =
                            ActionResult(
                                status = "queued",
                                message = "Action queued for execution",
                                actionId = pending.actionId,
                                requestId = requestId,
                            )
                        newActions.add(AIOpMode.RunningAction(action, requestId))
                    } else {
                        pending.resultData =
                            ActionResult(
                                status = "failed",
                                message = "Action not found: ${pending.actionId}",
                                actionId = pending.actionId,
                                requestId = requestId,
                            )
                    }
                } catch (e: ActionRegistry.ParameterValidationException) {
                    pending.resultData =
                        ActionResult(
                            status = "failed",
                            message = "Validation error: ${e.message}",
                            actionId = pending.actionId,
                            requestId = requestId,
                        )
                } catch (e: Exception) {
                    pending.resultData =
                        ActionResult(
                            status = "failed",
                            message = "Execution error: ${e.message}",
                            actionId = pending.actionId,
                            requestId = requestId,
                        )
                }
            }
        }
        return newActions
    }

    /**
     * Gets the result of a previously submitted action execution.
     *
     * @param requestId the client-assigned ID for the execution request
     * @return the result, or null if not found or still pending
     */
    fun getResult(requestId: String): ActionResult? {
        return pendingExecutions[requestId]?.resultData
    }

    /**
     * Resolves the result of a pending action.
     *
     * @param requestId the client-assigned ID for the execution request
     * @param success whether the action completed successfully
     * @param message a human-readable status message
     */
    fun resolveAction(requestId: String, success: Boolean, message: String) {
        pendingExecutions[requestId]?.resultData =
            ActionResult(
                status = if (success) "completed" else "failed",
                message = message,
                requestId = requestId,
            )
    }

    override fun serve(session: IHTTPSession): Response {
        val method = session.method
        val uri = session.uri

        return when (method) {
            Method.GET -> handleGet(uri)
            Method.POST -> handlePost(session)
            else ->
                newFixedLengthResponse(
                    Response.Status.METHOD_NOT_ALLOWED,
                    MIME_PLAINTEXT,
                    "Method not supported",
                )
        }
    }

    private fun handlePost(session: IHTTPSession): Response {
        val inputStream = session.inputStream
        val requestBody = inputStream.reader().use { it.readText() }

        val json = gson.fromJson(requestBody, JsonObject::class.java)
        val actionId = json.get("id").asString ?: return errorResponse("Missing 'id' field")
        val paramsRaw = json.get("params")?.asJsonObject ?: JsonObject()
        @Suppress("UNCHECKED_CAST")
        val params: Map<String, Any?> =
            gson.fromJson(paramsRaw, Map::class.java) as? Map<String, Any?> ?: emptyMap()
        val requestId = json.get("requestId")?.asString ?: java.util.UUID.randomUUID().toString()

        // If a result already exists for this requestId, return it (idempotent)
        val existing = pendingExecutions.get(requestId)
        if (existing != null) {
            val rd = existing.resultData
            if (rd != null) {
                return jsonResponse(gson.toJson(rd))
            }
        }

        // Queue the execution
        pendingExecutions[requestId] = ActionPending(actionId, params, requestId)

        return jsonResponse(
            gson.toJson(
                ActionResult(
                    status = "queued",
                    message = "Action queued",
                    actionId = actionId,
                    requestId = requestId,
                )
            )
        )
    }

    private fun handleGet(uri: String): Response =
        when {
            uri == "/api/actions" -> handleGetActions()
            uri == "/api/state" -> handleGetState()
            uri.startsWith("/api/result/") -> {
                val requestId = uri.removePrefix("/api/result/")
                handleGetResult(requestId)
            }
            uri == "/api/stream" -> createStreamResponse()
            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found")
        }

    private fun handleGetActions(): Response {
        val tools = ActionRegistry.toAITools()
        val descriptors = tools.map { tool ->
            mapOf(
                "name" to tool.name,
                "description" to tool.description,
                "inputSchema" to tool.inputSchema,
            )
        }
        return jsonResponse(gson.toJson(mapOf("actions" to descriptors)))
    }

    private fun handleGetState(): Response {
        val stateMessage = stateProvider?.invoke() ?: "No state available"
        return jsonResponse(gson.toJson(mapOf("state" to stateMessage)))
    }

    private fun handleGetResult(requestId: String): Response {
        val result = getResult(requestId)
        return if (result != null) {
            jsonResponse(gson.toJson(result))
        } else {
            newFixedLengthResponse(
                Response.Status.NOT_FOUND,
                "application/json",
                gson.toJson(mapOf("error" to "Result not found")),
            )
        }
    }

    private fun createStreamResponse(): Response {
        val pipedInput = PipedInputStream()
        val pipedOutput = PipedOutputStream(pipedInput)

        // Background thread sends SSE keepalive messages
        Thread {
            try {
                while (true) {
                    val keepalive = "event: keepalive\ndata: {}\n\n".toByteArray()
                    pipedOutput.write(keepalive)
                    pipedOutput.flush()
                    Thread.sleep(15000)
                }
            } catch (_: Exception) {
                try {
                    pipedOutput.close()
                } catch (_: Exception) {}
            }
        }
            .apply {
                isDaemon = true
                name = "sse-keepalive"
            }
            .start()

        return newChunkedResponse(Response.Status.OK, "text/event-stream", pipedInput)
    }

    private fun jsonResponse(body: String): Response =
        newFixedLengthResponse(Response.Status.OK, "application/json", body)

    private fun errorResponse(message: String): Response =
        jsonResponse(gson.toJson(mapOf("error" to message)))

    /** Shuts down the execution thread pool. */
    fun shutdown() {
        executionPool.shutdown()
    }
}

/**
 * Result of an action execution.
 *
 * @property status the execution status: "queued", "completed", or "failed"
 * @property message a human-readable status message
 * @property data optional additional data returned by the action
 * @property actionId the ID of the action that was executed (for tracking)
 * @property requestId the client-assigned ID for this execution request
 */
data class ActionResult(
    val status: String,
    val message: String,
    val data: Map<String, Any?>? = null,
    val actionId: String? = null,
    val requestId: String? = null,
)

/**
 * Pending action execution request.
 *
 * @property actionId the ID of the action to execute
 * @property params the parameters to pass to the action
 * @property requestId the client-assigned ID for tracking
 * @property resultData the result once execution completes (null until done)
 */
data class ActionPending(
    val actionId: String,
    val params: Map<String, Any?>,
    val requestId: String,
    var resultData: ActionResult? = null,
)
