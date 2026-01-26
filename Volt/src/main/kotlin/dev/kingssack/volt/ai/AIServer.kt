package dev.kingssack.volt.ai

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import fi.iki.elonen.NanoWSD
import java.io.IOException

class AIServer(port: Int = 8081) : NanoWSD(port) {
    private val gson = Gson()
    private var activeSocket: WebSocket? = null
    private var actionCallback: ((String, Map<String, Any?>) -> ActionResult)? = null

    companion object {
        private const val TAG = "VoltAIServer"
    }

    data class ActionResult(
        val success: Boolean,
        val message: String,
        val data: Map<String, Any?>? = null,
    )

    fun setActionCallback(callback: (String, Map<String, Any?>) -> ActionResult) {
        actionCallback = callback
    }

    override fun openWebSocket(handshake: IHTTPSession?): WebSocket? {
        return AIWebSocket(handshake)
    }

    inner class AIWebSocket(handshake: IHTTPSession?) : WebSocket(handshake) {
        override fun onOpen() {
            Log.i(TAG, "AI Client connected")
            activeSocket = this

            // Send available tools on connection
            val tools = ActionRegistry.toAITools()
            val response = mapOf("type" to "tools", "tools" to tools)
        }

        override fun onMessage(message: WebSocketFrame) {
            try {
                val json = gson.fromJson(message.textPayload, JsonObject::class.java)
                val type = json.get("type")?.asString

                when (type) {
                    "execute" -> handleExecute(json)
                    "get_tools" -> handleGetTools()
                    "get_state" -> handleGetState()
                    else -> sendError("Unknown message type: $type")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing message", e)
                sendError(e.message ?: "Unknown error")
            }
        }

        private fun handleExecute(json: JsonObject) {
            val actionId = json.get("action_id")?.asString ?: return sendError("Missing action_id")
            val params =
                json.get("params")?.asJsonObject?.let {
                    gson.fromJson<Map<String, Any?>>(it, Map::class.java)
                } ?: emptyMap()

            val result =
                actionCallback?.invoke(actionId, params)
                    ?: ActionResult(false, "No action handler registered")

            val response =
                mapOf(
                    "type" to "result",
                    "action_id" to actionId,
                    "success" to result.success,
                    "messages" to result.message,
                    "data" to result.data,
                )
            send(gson.toJson(response))
        }

        private fun handleGetTools() {
            val tools = ActionRegistry.toAITools()
            val response = mapOf("type" to "tools", "tools" to tools)
            send(gson.toJson(response))
        }

        private fun handleGetState() {
            val response =
                mapOf(
                    "type" to "state",
                    "state" to mapOf<String, Any?>(), // Placeholder for actual state data
                )
            send(gson.toJson(response))
        }

        private fun sendError(message: String) {
            val response = mapOf("type" to "error", "message" to message)
            send(gson.toJson(response))
        }

        override fun onClose(
            code: WebSocketFrame.CloseCode?,
            reason: String?,
            initiatedByRemote: Boolean,
        ) {
            Log.i(TAG, "AI Client disconnected: $reason")
            if (activeSocket == this) activeSocket = null
        }

        override fun onPong(pong: WebSocketFrame?) {}

        override fun onException(exception: IOException?) {
            Log.e(TAG, "WebSocket error", exception)
        }
    }

    fun broadcastState(state: Map<String, Any>) {
        activeSocket?.let { socket ->
            val response = mapOf("type" to "state", "state" to state)
            socket.send(gson.toJson(response))
        }
    }
}
