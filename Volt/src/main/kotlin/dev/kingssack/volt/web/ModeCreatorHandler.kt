package dev.kingssack.volt.web

import android.util.Log
import com.google.gson.Gson
import fi.iki.elonen.NanoHTTPD
import org.firstinspires.ftc.robotcore.internal.webserver.WebHandler

/**
 * Handler for the mode creator API endpoints.
 */
class ModeCreatorHandler : WebHandler {
    companion object {
        private const val TAG = "ModeCreatorHandler"
        private val gson = Gson()

        // In-memory storage for created modes (in a real implementation, this would be persisted)
        private val autonomousModes = mutableListOf<AutonomousModeConfig>()
        private val manualModes = mutableListOf<ManualModeConfig>()
    }

    /**
     * Configuration for an autonomous mode.
     */
    data class AutonomousModeConfig(
        val name: String,
        val robot: String,
        val sequence: List<ActionConfig>
    )

    /**
     * Configuration for a manual mode.
     */
    data class ManualModeConfig(
        val name: String,
        val robot: String,
        val mappings: List<MappingConfig>
    )

    /**
     * Configuration for an action.
     */
    data class ActionConfig(
        val id: String,
        val name: String,
        val description: String,
        val parameters: List<ParameterConfig>
    )

    /**
     * Configuration for a parameter.
     */
    data class ParameterConfig(
        val name: String,
        val type: String,
        val value: Any
    )

    /**
     * Configuration for a control mapping.
     */
    data class MappingConfig(
        val action: ActionConfig,
        val controlType: String,
        val control: String
    )

    override fun getResponse(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val uri = session.uri.removePrefix("/volt")
        val method = session.method

        Log.d(TAG, "Handling request: $method $uri")

        return when {
            method == NanoHTTPD.Method.GET && uri == "/api/robots" -> handleGetRobots()
            method == NanoHTTPD.Method.GET && uri == "/api/actions" -> handleGetActions(session)
            method == NanoHTTPD.Method.GET && uri == "/api/autonomous-modes" -> handleGetAutonomousModes()
            method == NanoHTTPD.Method.POST && uri == "/api/autonomous-modes" -> handleCreateAutonomousMode(session)
            method == NanoHTTPD.Method.GET && uri == "/api/manual-modes" -> handleGetManualModes()
            method == NanoHTTPD.Method.POST && uri == "/api/manual-modes" -> handleCreateManualMode(session)
            else -> NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.NOT_FOUND,
                NanoHTTPD.MIME_PLAINTEXT,
                "Not found"
            )
        }
    }

    /**
     * Handle GET /api/robot
     */
    private fun handleGetRobots(): NanoHTTPD.Response {
        val robots = listOf<String>()
        // TODO: Get all available robots
        return createJsonResponse(gson.toJson(robots))
    }

    /**
     * Handle GET /api/actions
     */
    private fun handleGetActions(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val robot = session.parameters["robot"]?.get(0)

        // Get available actions for the specified robot type
        val actions = when (robot) {
            // TODO: Get actions based on the robot type
            else -> emptyList<String>()
        }

        return createJsonResponse(gson.toJson(actions))
    }

    /**
     * Handle GET /api/autonomous-modes
     */
    private fun handleGetAutonomousModes(): NanoHTTPD.Response {
        return createJsonResponse(gson.toJson(autonomousModes))
    }

    /**
     * Handle POST /api/autonomous-modes
     */
    private fun handleCreateAutonomousMode(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val config = parseRequestBody<AutonomousModeConfig>(session)
        if (config != null) {
            autonomousModes.add(config)

            // Create and register the autonomous mode
            ModeCreator.createAndRegisterAutonomousMode(config)

            return createJsonResponse(gson.toJson(config))
        }

        return NanoHTTPD.newFixedLengthResponse(
            NanoHTTPD.Response.Status.BAD_REQUEST,
            NanoHTTPD.MIME_PLAINTEXT,
            "Invalid request body"
        )
    }

    /**
     * Handle GET /api/manual-modes
     */
    private fun handleGetManualModes(): NanoHTTPD.Response {
        return createJsonResponse(gson.toJson(manualModes))
    }

    /**
     * Handle POST /api/manual-modes
     */
    private fun handleCreateManualMode(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val config = parseRequestBody<ManualModeConfig>(session)
        if (config != null) {
            manualModes.add(config)

            // Create and register the manual mode
            ModeCreator.createAndRegisterManualMode(config)

            return createJsonResponse(gson.toJson(config))
        }

        return NanoHTTPD.newFixedLengthResponse(
            NanoHTTPD.Response.Status.BAD_REQUEST,
            NanoHTTPD.MIME_PLAINTEXT,
            "Invalid request body"
        )
    }

    /**
     * Parse the request body as JSON and convert it to the specified type.
     */
    private inline fun <reified T> parseRequestBody(session: NanoHTTPD.IHTTPSession): T? {
        val contentLength = session.headers["content-length"]?.toIntOrNull() ?: 0
        val buffer = ByteArray(contentLength)

        try {
            session.inputStream.read(buffer, 0, contentLength)
            val json = String(buffer)
            return gson.fromJson(json, T::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing request body", e)
            return null
        }
    }

    /**
     * Create a JSON response.
     */
    private fun createJsonResponse(json: String): NanoHTTPD.Response {
        return NanoHTTPD.newFixedLengthResponse(
            NanoHTTPD.Response.Status.OK,
            "application/json",
            json
        )
    }
}
