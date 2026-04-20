package dev.kingssack.volt.ai

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import fi.iki.elonen.NanoWSD
import java.io.IOException

/**
 * WebSocket server for AI client communication.
 *
 * Provides a WebSocket interface for AI agents to execute robot actions and receive state updates.
 * Supports request-response correlation via request IDs and provides heartbeat functionality for
 * connection health monitoring.
 *
 * @param port the port to listen on (default: 8081)
 */
class AIServer(port: Int = 8081) : NanoWSD(port) {
    private val gson = Gson()
    private var activeSocket: AIWebSocket? = null
    private var actionCallback: ((String, Map<String, Any?>, String?) -> ActionResult)? = null
    private var stateProvider: (() -> Map<String, Any>)? = null

    companion object {
        private const val TAG = "VoltAIServer"
        private const val PING_INTERVAL_MS = 5000L
    }

    /**
     * Result of an action execution.
     *
     * @property success whether the action was successfully queued/executed
     * @property message a human-readable status message
     * @property data optional additional data returned by the action
     * @property actionId the ID of the action that was executed (for tracking)
     */
    data class ActionResult(
        val success: Boolean,
        val message: String,
        val data: Map<String, Any?>? = null,
        val actionId: String? = null,
    )

    /**
     * Sets the [callback] for handling action execution requests.
     *
     * @param callback function that receives (actionId, params, requestId) and returns an
     *   [ActionResult]
     */
    fun setActionCallback(callback: (String, Map<String, Any?>, String?) -> ActionResult) {
        actionCallback = callback
    }

    /**
     * Sets the [provider] for robot state data.
     *
     * This [provider] is called when a client requests the current state via the `get_state`
     * message.
     *
     * @param provider Function that returns the current robot state as a map
     */
    fun setStateProvider(provider: () -> Map<String, Any>) {
        stateProvider = provider
    }

    override fun openWebSocket(handshake: IHTTPSession?): WebSocket = AIWebSocket(handshake)

    /**
     * Sends a ping to the active client to check connection health.
     *
     * Should be called periodically (e.g., every 5 seconds) to detect stale connections.
     */
    fun sendPing() {
        val socket = activeSocket ?: return
        try {
            socket.ping(ByteArray(0))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send ping", e)
            clearActiveSocket(socket)
        }
    }

    private fun clearActiveSocket(socket: AIWebSocket) {
        if (activeSocket == socket) {
            activeSocket = null
        }
    }

    inner class AIWebSocket(handshake: IHTTPSession?) : WebSocket(handshake) {
        private var lastPongTime = System.currentTimeMillis()

        override fun onOpen() {
            Log.i(TAG, "AI Client connected")
            activeSocket = this
            lastPongTime = System.currentTimeMillis()

            try {
                // Send available tools on connection
                val tools = ActionRegistry.toAITools()
                val response = mapOf("type" to "tools", "tools" to tools)
                send(gson.toJson(response))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send tools", e)
            }
        }

        override fun onMessage(message: WebSocketFrame) {
            try {
                val json = gson.fromJson(message.textPayload, JsonObject::class.java)
                val requestId = json.get("request_id")?.asString

                when (val type = json.get("type")?.asString) {
                    "execute" -> handleExecute(json, requestId)
                    "get_tools" -> handleGetTools(requestId)
                    "get_state" -> handleGetState(requestId)
                    else -> sendError("Unknown message type: $type", requestId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing message", e)
                sendError(e.message ?: "Unknown error", null)
            }
        }

        private fun handleExecute(json: JsonObject, requestId: String?) {
            val actionId =
                json.get("action_id")?.asString ?: return sendError("Missing action_id", requestId)
            val params =
                json.get("params")?.asJsonObject?.let {
                    gson.fromJson<Map<String, Any?>>(it, Map::class.java)
                } ?: emptyMap()

            val result =
                actionCallback?.invoke(actionId, params, requestId)
                    ?: ActionResult(false, "No action handler registered")

            val response = buildMap {
                put("type", "result")
                put("action_id", actionId)
                put("success", result.success)
                put("message", result.message)
                result.data?.let { put("data", it) }
                requestId?.let { put("request_id", it) }
            }
            send(gson.toJson(response))
        }

        private fun handleGetTools(requestId: String?) {
            val tools = ActionRegistry.toAITools()
            val response = buildMap {
                put("type", "tools")
                put("tools", tools)
                requestId?.let { put("request_id", it) }
            }
            send(gson.toJson(response))
        }

        private fun handleGetState(requestId: String?) {
            val state = stateProvider?.invoke() ?: mapOf()
            val response = buildMap {
                put("type", "state")
                put("state", state)
                requestId?.let { put("request_id", it) }
            }
            send(gson.toJson(response))
        }

        private fun sendError(message: String, requestId: String?) {
            val response = buildMap {
                put("type", "error")
                put("message", message)
                requestId?.let { put("request_id", it) }
            }
            send(gson.toJson(response))
        }

        override fun onClose(
            code: WebSocketFrame.CloseCode?,
            reason: String?,
            initiatedByRemote: Boolean,
        ) {
            Log.i(TAG, "AI Client disconnected: $reason")
            clearActiveSocket(this)
        }

        override fun onPong(pong: WebSocketFrame?) {
            lastPongTime = System.currentTimeMillis()
            Log.d(TAG, "Pong received - connection alive")
        }

        override fun onException(exception: IOException?) {
            Log.e(TAG, "WebSocket error", exception)
            clearActiveSocket(this)
        }

        /** Returns whether the connection is considered alive based on recent pong responses. */
        fun isConnectionAlive(): Boolean =
            System.currentTimeMillis() - lastPongTime < PING_INTERVAL_MS * 3
    }

    /**
     * Broadcasts the current robot state to the connected client.
     *
     * @param state the state data to broadcast
     */
    fun broadcastState(state: Map<String, Any>) {
        val socket = activeSocket ?: return
        try {
            val response = mapOf("type" to "state", "state" to state)
            socket.send(gson.toJson(response))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to broadcast state", e)
            clearActiveSocket(socket)
        }
    }

    /**
     * Broadcasts an action completion notification to the connected client.
     *
     * @param actionId the ID of the completed action
     * @param requestId the original request ID (for correlation)
     * @param success whether the action completed successfully
     * @param message a status message about the completion
     * @param data optional result data from the action
     */
    fun broadcastActionComplete(
        actionId: String,
        requestId: String?,
        success: Boolean,
        message: String,
        data: Map<String, Any?>? = null,
    ) {
        val socket = activeSocket ?: return
        try {
            val response = buildMap {
                put("type", "action_complete")
                put("action_id", actionId)
                put("success", success)
                put("message", message)
                data?.let { put("data", it) }
                requestId?.let { put("request_id", it) }
            }
            socket.send(gson.toJson(response))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to broadcast action completion", e)
            clearActiveSocket(socket)
        }
    }

    /** Returns whether there is an active, healthy client connection. */
    fun hasActiveConnection(): Boolean = activeSocket?.isConnectionAlive() == true
}
