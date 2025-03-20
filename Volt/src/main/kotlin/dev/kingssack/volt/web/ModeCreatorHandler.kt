package dev.kingssack.volt.web

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.kingssack.volt.opmode.CustomOpModes
import dev.kingssack.volt.opmode.autonomous.AutonomousMode
import dev.kingssack.volt.opmode.manual.ManualMode
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.robot.RobotWithMecanumDrive
import fi.iki.elonen.NanoHTTPD
import org.firstinspires.ftc.robotcore.internal.webserver.WebHandler
import java.io.File
import java.io.IOException
import java.lang.reflect.Method

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
        val robotType: String,
        val sequence: List<ActionConfig>
    )

    /**
     * Configuration for a manual mode.
     */
    data class ManualModeConfig(
        val name: String,
        val robotType: String,
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
            method == NanoHTTPD.Method.GET && uri == "/api/robot-types" -> handleGetRobotTypes()
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
     * Handle GET /api/robot-types
     */
    private fun handleGetRobotTypes(): NanoHTTPD.Response {
        val robotTypes = listOf("RobotWithMecanumDrive")
        return createJsonResponse(gson.toJson(robotTypes))
    }

    /**
     * Handle GET /api/actions
     */
    private fun handleGetActions(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val robotType = session.parameters["robotType"]?.get(0) ?: "RobotWithMecanumDrive"

        // Get available actions for the specified robot type
        val actions = when (robotType) {
            "RobotWithMecanumDrive" -> getRobotWithMecanumDriveActions()
            else -> emptyList()
        }

        return createJsonResponse(gson.toJson(actions))
    }

    /**
     * Get actions available for RobotWithMecanumDrive.
     */
    private fun getRobotWithMecanumDriveActions(): List<ActionConfig> {
        return listOf(
            ActionConfig(
                id = "pathTo",
                name = "Path To",
                description = "Move the robot to a specific position",
                parameters = listOf(
                    ParameterConfig("x", "number", 0),
                    ParameterConfig("y", "number", 0),
                    ParameterConfig("heading", "number", 0)
                )
            ),
            ActionConfig(
                id = "wait",
                name = "Wait",
                description = "Wait for a specified time",
                parameters = listOf(
                    ParameterConfig("seconds", "number", 1)
                )
            )
        )
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
