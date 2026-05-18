package dev.kingssack.volt.ai

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import fi.iki.elonen.NanoHTTPD
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors

/**
 * A REST API for controlling a robot with an AI agent.
 *
 * Provides endpoints for listing available actions, retrieving robot state, and executing actions.
 * Actions are executed via a cached thread pool to support parallel execution.
 *
 * @param port the port to listen on (default: 8081)
 */
class AIServer(private val registry: ActionRegistry, port: Int = 8081) : NanoHTTPD(port) {
    private val TAG = "VoltAIServer"
    private val gson = Gson()

    private val pendingExecutions = ConcurrentHashMap<String, ActionPending>()
    private val executionPool = Executors.newCachedThreadPool { runnable ->
        val thread = Thread(runnable, "ai-action-executor")
        thread.isDaemon = true
        thread
    }

    private val queuedActions = ConcurrentLinkedQueue<AIOpMode.RunningAction>()

    /** Callback to retrieve the current robot state message. Set by [AIOpMode] each tick. */
    var stateProvider: (() -> String)? = null

    init {
        start(SOCKET_READ_TIMEOUT, false)
        Log.d(TAG, "Server started on port $port")
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

        // 1. Submit pending executions to the background threads
        for ((requestId, pending) in snapshot) {
            if (pending.resultData != null) continue
            executionPool.submit {
                try {
                    val tool = registry.actions[pending.name]
                    val action = tool?.invoke(pending.params)
                    if (action != null) {
                        pending.resultData =
                            ActionResult(
                                status = "queued",
                                message = "Action queued for execution",
                                name = pending.name,
                                requestId = requestId,
                            )
                        Log.d(TAG, "Executing action: ${pending.name}, params: ${pending.params}")
                        queuedActions.add(AIOpMode.RunningAction(action, requestId))
                    } else {
                        pending.resultData =
                            ActionResult(
                                status = "failed",
                                message = "Action not found: ${pending.name}",
                                name = pending.name,
                                requestId = requestId,
                            )
                    }
                } catch (e: ActionRegistry.ParameterValidationException) {
                    pending.resultData =
                        ActionResult(
                            status = "failed",
                            message = "Validation error: ${e.message}",
                            name = pending.name,
                            requestId = requestId,
                        )
                } catch (e: Exception) {
                    pending.resultData =
                        ActionResult(
                            status = "failed",
                            message = "Execution error: ${e.message}",
                            name = pending.name,
                            requestId = requestId,
                        )
                }
            }
        }

        // 2. Safely drain whatever has finished processing into a list
        val newActions = mutableListOf<AIOpMode.RunningAction>()
        while (queuedActions.isNotEmpty()) {
            val action = queuedActions.poll()
            if (action != null) newActions.add(action)
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
        // 1. Safely read based on Content-Length to avoid keep-alive deadlocks
        val contentLength = session.headers["content-length"]?.toIntOrNull() ?: 0
        if (contentLength <= 0) {
            return errorResponse("Missing Content-Length or empty request body")
        }

        val buffer = ByteArray(contentLength)
        var bytesRead = 0
        val inputStream = session.inputStream
        while (bytesRead < contentLength) {
            val read = inputStream.read(buffer, bytesRead, contentLength - bytesRead)
            if (read == -1) break
            bytesRead += read
        }

        val requestBody = String(buffer, Charsets.UTF_8)

        // 2. Parse the JSON
        val json =
            try {
                gson.fromJson(requestBody, JsonObject::class.java)
            } catch (e: Exception) {
                return errorResponse("Invalid JSON provided")
            }

        val name = json.get("name")?.asString ?: return errorResponse("Missing 'name' field")
        val paramsRaw = json.get("params")?.asJsonObject ?: JsonObject()
        @Suppress("UNCHECKED_CAST")
        val params: Map<String, Any?> =
            gson.fromJson(paramsRaw, Map::class.java) as? Map<String, Any?> ?: emptyMap()
        val requestId = json.get("requestId")?.asString ?: UUID.randomUUID().toString()

        Log.d(
            TAG,
            "Received action execution request: $name, params: $params, requestId: $requestId",
        )

        // If a result already exists for this requestId, return it (idempotent)
        val existing = pendingExecutions[requestId]
        if (existing != null && existing.resultData != null)
            return jsonResponse(gson.toJson(existing.resultData))

        // Queue the execution
        pendingExecutions[requestId] = ActionPending(name, params, requestId)

        return jsonResponse(
            gson.toJson(
                ActionResult(
                    status = "queued",
                    message = "Action queued",
                    name = name,
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
        Log.d(TAG, "Sending action list")
        val descriptors =
            registry.actions.map { (_, tool) ->
                mapOf(
                    "name" to tool.name,
                    "description" to tool.description,
                    "parameterSchema" to tool.parameters,
                )
            }
        return jsonResponse(gson.toJson(mapOf("actions" to descriptors)))
    }

    private fun handleGetState(): Response {
        Log.d(TAG, "Sending robot state")
        val stateMessage = stateProvider?.invoke() ?: "No state available"
        return jsonResponse(gson.toJson(mapOf("state" to stateMessage)))
    }

    private fun handleGetResult(requestId: String): Response {
        Log.d(TAG, "Sending result for requestId: $requestId")
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
 * @property name the name of the action that was executed
 * @property requestId the client-assigned ID for this execution request
 */
data class ActionResult(
    val status: String,
    val message: String,
    val data: Map<String, Any?>? = null,
    val name: String? = null,
    val requestId: String? = null,
)

/**
 * Pending action execution request.
 *
 * @property name the name of the action being executed
 * @property params the parameters used to build the action
 * @property requestId the client-assigned ID for tracking
 * @property resultData the result once execution completes (null until done)
 */
data class ActionPending(
    val name: String,
    val params: Map<String, Any?>,
    val requestId: String,
    var resultData: ActionResult? = null,
)
