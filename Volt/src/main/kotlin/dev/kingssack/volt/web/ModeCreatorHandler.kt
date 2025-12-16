package dev.kingssack.volt.web

import android.util.Log
import com.acmerobotics.roadrunner.Action
import com.google.gson.Gson
import dev.kingssack.volt.attachment.Attachment
import dev.kingssack.volt.robot.Robot
import fi.iki.elonen.NanoHTTPD
import java.lang.reflect.Modifier
import org.firstinspires.ftc.robotcore.internal.webserver.WebHandler

/** Handler for the mode creator API endpoints. */
class ModeCreatorHandler : WebHandler {
    companion object {
        private const val TAG = "ModeCreatorHandler"
        private val gson = Gson()

        // In-memory storage for created modes (in a real implementation, this would be persisted)
        private val autonomousModes = mutableListOf<AutonomousModeConfig>()
        private val manualModes = mutableListOf<ManualModeConfig>()
    }

    /** Configuration for an autonomous mode. */
    data class AutonomousModeConfig(
            val name: String,
            val robot: String,
            val sequence: List<ActionConfig>
    )

    /** Configuration for a manual mode. */
    data class ManualModeConfig(
            val name: String,
            val robot: String,
            val mappings: List<MappingConfig>
    )

    /** Configuration for an action. */
    data class ActionConfig(
            val id: String,
            val name: String,
            val description: String,
            val parameters: List<ParameterConfig>
    )

    /** Configuration for a parameter. */
    data class ParameterConfig(val name: String, val type: String, val value: Any)

    /** Configuration for a control mapping. */
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
            method == NanoHTTPD.Method.GET && uri == "/api/attachments" -> handleGetAttachments()
            method == NanoHTTPD.Method.GET && uri == "/api/autonomous-modes" ->
                    handleGetAutonomousModes()
            method == NanoHTTPD.Method.POST && uri == "/api/autonomous-modes" ->
                    handleCreateAutonomousMode(session)
            method == NanoHTTPD.Method.GET && uri == "/api/manual-modes" -> handleGetManualModes()
            method == NanoHTTPD.Method.POST && uri == "/api/manual-modes" ->
                    handleCreateManualMode(session)
            method == NanoHTTPD.Method.GET && uri == "/api/actions" -> handleGetActions(session)
            else ->
                    NanoHTTPD.newFixedLengthResponse(
                            NanoHTTPD.Response.Status.NOT_FOUND,
                            NanoHTTPD.MIME_PLAINTEXT,
                            "Not found"
                    )
        }
    }

    /** Handle GET /api/robots */
    private fun handleGetRobots(): NanoHTTPD.Response {
        val robots = getRobotClasses().map { it.simpleName }
        return createJsonResponse(gson.toJson(robots))
    }

    /** Handle GET /api/attachments */
    private fun handleGetAttachments(): NanoHTTPD.Response {
        val attachments = getAttachmentClasses().map { it.simpleName }
        return createJsonResponse(gson.toJson(attachments))
    }

    /** Handle GET /api/actions */
    private fun handleGetActions(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val attachments = getAttachmentClasses()

        val actions = mutableListOf<ActionConfig>()

        for (clazz in attachments) {
            clazz.declaredMethods.forEach { method ->
                if (Action::class.java.isAssignableFrom(method.returnType) &&
                                Modifier.isPublic(method.modifiers)
                ) {
                    val params =
                            method.parameterTypes.mapIndexed { index, type ->
                                ParameterConfig(
                                        name = "arg$index",
                                        type = type.simpleName,
                                        value = getDefaultValue(type)
                                )
                            }

                    actions.add(
                            ActionConfig(
                                    id = method.name,
                                    name = "${clazz.simpleName}.${method.name}",
                                    description = "Action from ${clazz.simpleName}",
                                    parameters = params
                            )
                    )
                }
            }
        }

        return createJsonResponse(gson.toJson(actions))
    }

    /** Handle GET /api/autonomous-modes */
    private fun handleGetAutonomousModes(): NanoHTTPD.Response {
        return createJsonResponse(gson.toJson(autonomousModes))
    }

    /** Handle POST /api/autonomous-modes */
    private fun handleCreateAutonomousMode(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val config = parseRequestBody<AutonomousModeConfig>(session)
        if (config != null) {
            autonomousModes.add(config)
            return createJsonResponse(gson.toJson(config))
        }
        return NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.BAD_REQUEST,
                NanoHTTPD.MIME_PLAINTEXT,
                "Invalid request body"
        )
    }

    /** Handle GET /api/manual-modes */
    private fun handleGetManualModes(): NanoHTTPD.Response {
        return createJsonResponse(gson.toJson(manualModes))
    }

    /** Handle POST /api/manual-modes */
    private fun handleCreateManualMode(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val config = parseRequestBody<ManualModeConfig>(session)
        if (config != null) {
            manualModes.add(config)
            return createJsonResponse(gson.toJson(config))
        }
        return NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.BAD_REQUEST,
                NanoHTTPD.MIME_PLAINTEXT,
                "Invalid request body"
        )
    }

    /** Parse the request body as JSON and convert it to the specified type. */
    private inline fun <reified T> parseRequestBody(session: NanoHTTPD.IHTTPSession): T? {
        val contentLength = session.headers["content-length"]?.toIntOrNull() ?: 0
        if (contentLength == 0) return null

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

    /** Create a JSON response. */
    private fun createJsonResponse(json: String): NanoHTTPD.Response {
        return NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.OK,
                "application/json",
                json
        )
    }

    private fun getDefaultValue(type: Class<*>): Any {
        return when (type) {
            Int::class.java, Integer::class.java -> 0
            Double::class.java, java.lang.Double::class.java -> 0.0
            Float::class.java, java.lang.Float::class.java -> 0.0f
            Boolean::class.java, java.lang.Boolean::class.java -> false
            String::class.java -> ""
            else -> "null"
        }
    }

    private fun getAttachmentClasses(): List<Class<out Attachment>> {
        // Get attachment classes
        return emptyList()
    }

    private fun getRobotClasses(): List<Class<out Robot>> {
        // Get robot classes
        return emptyList()
    }
}
