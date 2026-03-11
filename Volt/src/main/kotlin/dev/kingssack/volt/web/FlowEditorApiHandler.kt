package dev.kingssack.volt.web

import android.util.Log
import com.google.gson.Gson
import dev.kingssack.volt.service.MetadataService
import fi.iki.elonen.NanoHTTPD
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.mapOf
import org.firstinspires.ftc.robotcore.internal.webserver.WebHandler

/**
 * Web handler for the Volt Flow Editor API endpoints.
 *
 * This handler provides a RESTful API for managing OpModes, discovering actions and robots,
 * validating flow graphs, and generating Kotlin code.
 */
class FlowEditorApiHandler : WebHandler {
    companion object {
        private const val TAG = "FlowEditorApiHandler"
        private val gson = Gson()

        // Thread-safe cache of OpModes (persisted to files)
        private val opModes = ConcurrentHashMap<String, OpModeDefinition>()
    }

    override fun getResponse(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val uri = session.uri.removePrefix("/volt")
        val method = session.method

        Log.d(TAG, "Handling request: $method $uri")

        return when (method) {
            NanoHTTPD.Method.GET if uri == "/api/opmodes" ->
                if (session.getQueryParameter("id") != null) handleGetOpMode(session)
                else handleGetOpModes()

            NanoHTTPD.Method.POST if uri == "/api/opmodes" -> handleCreateOpMode(session)
            NanoHTTPD.Method.PUT if uri == "/api/opmodes" -> handleUpdateOpMode(session)
            NanoHTTPD.Method.DELETE if uri == "/api/opmodes" -> handleDeleteOpMode(session)

            NanoHTTPD.Method.GET if uri == "/api/robots" ->
                if (session.getQueryParameter("id") != null) handleGetRobot(session)
                else handleGetRobots()
            NanoHTTPD.Method.GET if uri == "/api/robots/actions" -> handleGetRobotActions(session)

            NanoHTTPD.Method.GET if uri == "/api/editor-capabilities" ->
                handleGetEditorCapabilities(session)

            NanoHTTPD.Method.GET if uri == "/api/actions" -> handleGetAction(session)
            NanoHTTPD.Method.GET if uri == "/api/events" -> handleGetEvent(session)

            NanoHTTPD.Method.POST if uri == "/api/validate" -> handleValidate(session)

            NanoHTTPD.Method.POST if uri == "/api/generate" -> handleGenerateCode(session)
            else -> createErrorResponse("Not found", NanoHTTPD.Response.Status.NOT_FOUND)
        }
    }

    /** Handle GET /api/opmodes */
    private fun handleGetOpModes(): NanoHTTPD.Response {
        val opModeList =
            opModes.values.map { opMode ->
                mapOf(
                    "id" to opMode.id,
                    "name" to opMode.name,
                    "type" to opMode.type,
                    "robotId" to opMode.robotId,
                )
            }
        return createJsonResponse(gson.toJson(opModeList))
    }

    /** Handle GET /api/opmodes?id=:id */
    private fun handleGetOpMode(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val id =
            session.getQueryParameter("id")
                ?: return createErrorResponse(
                    "Missing id",
                    NanoHTTPD.Response.Status.BAD_REQUEST,
                )
        val opMode =
            opModes[id]
                ?: return createErrorResponse(
                    "OpMode not found",
                    NanoHTTPD.Response.Status.NOT_FOUND,
                )
        return createJsonResponse(gson.toJson(opMode))
    }

    /** Handle POST /api/opmodes */
    private fun handleCreateOpMode(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        try {
            val opModeConfig =
                parseRequestBody<OpModeCreationConfig>(session)
                    ?: return createErrorResponse(
                        "Invalid request body",
                        NanoHTTPD.Response.Status.BAD_REQUEST,
                    )
            val opModeId = UUID.randomUUID().toString()

            val flowGraph = FlowGraph(nodes = emptyList(), connections = emptyList())

            val opMode =
                OpModeDefinition(
                    id = opModeId,
                    name = opModeConfig.name,
                    type = opModeConfig.type,
                    robotId = opModeConfig.robotId,
                    flowGraph = flowGraph,
                    constructorParams = opModeConfig.constructorParams,
                )

            opModes[opModeId] = opMode

            return createJsonResponse(gson.toJson(mapOf("id" to opModeId)))
        } catch (e: Exception) {
            Log.e(TAG, "Error creating OpMode", e)
            return createErrorResponse(
                "Error creating OpMode: ${e.message}",
                NanoHTTPD.Response.Status.BAD_REQUEST,
            )
        }
    }

    /** Handle PUT /api/opmodes?id=:id */
    private fun handleUpdateOpMode(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        try {
            val id =
                session.getQueryParameter("id")
                    ?: return createErrorResponse(
                        "Missing id",
                        NanoHTTPD.Response.Status.BAD_REQUEST,
                    )
            val existingOpMode =
                opModes[id]
                    ?: return createErrorResponse(
                        "OpMode not found",
                        NanoHTTPD.Response.Status.NOT_FOUND,
                    )
            val updatedOpMode =
                parseRequestBody<OpModeDefinition>(session)
                    ?: return createErrorResponse(
                        "Invalid request body",
                        NanoHTTPD.Response.Status.BAD_REQUEST,
                    )

            val persistedOpMode =
                updatedOpMode.copy(
                    id = existingOpMode.id,
                    generatedCode = updatedOpMode.generatedCode ?: existingOpMode.generatedCode,
                )
            opModes[id] = persistedOpMode

            return createJsonResponse(gson.toJson(persistedOpMode))
        } catch (e: Exception) {
            Log.e(TAG, "Error updating OpMode", e)
            return createErrorResponse(
                "Error updating OpMode: ${e.message}",
                NanoHTTPD.Response.Status.BAD_REQUEST,
            )
        }
    }

    /** Handle DELETE /api/opmodes?id=:id */
    private fun handleDeleteOpMode(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val id =
            session.getQueryParameter("id")
                ?: return createErrorResponse(
                    "Missing id",
                    NanoHTTPD.Response.Status.BAD_REQUEST,
                )
        if (opModes.remove(id) != null) {
            return createJsonResponse(gson.toJson(mapOf("success" to true)))
        }
        return createErrorResponse("OpMode not found", NanoHTTPD.Response.Status.NOT_FOUND)
    }

    /** Handle GET /api/robots */
    private fun handleGetRobots(): NanoHTTPD.Response {
        return try {
            val robots = MetadataService.getRobots()
            createJsonResponse(gson.toJson(robots))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting robots", e)
            createErrorResponse(
                "Error getting robots: ${e.message}",
                NanoHTTPD.Response.Status.INTERNAL_ERROR,
            )
        }
    }

    /** Handle GET /api/robots?id=:id */
    private fun handleGetRobot(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        return try {
            val id =
                session.getQueryParameter("id")
                    ?: return createErrorResponse(
                        "Missing id",
                        NanoHTTPD.Response.Status.BAD_REQUEST,
                    )

            val robot =
                MetadataService.getRobotById(id)
                    ?: return createErrorResponse(
                        "Robot not found",
                        NanoHTTPD.Response.Status.NOT_FOUND,
                    )

            createJsonResponse(gson.toJson(robot))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting robot", e)
            createErrorResponse(
                "Error getting robot: ${e.message}",
                NanoHTTPD.Response.Status.INTERNAL_ERROR,
            )
        }
    }

    /** Handle GET /api/robots/actions?id=:id */
    private fun handleGetRobotActions(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        return try {
            val id =
                session.getQueryParameter("id")
                    ?: return createErrorResponse(
                        "Missing id",
                        NanoHTTPD.Response.Status.BAD_REQUEST,
                    )

            val robot =
                MetadataService.getRobotById(id)
                    ?: return createErrorResponse(
                        "Robot not found",
                        NanoHTTPD.Response.Status.NOT_FOUND,
                    )

            createJsonResponse(gson.toJson(robot.actions))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting robot actions", e)
            createErrorResponse("Error: ${e.message}", NanoHTTPD.Response.Status.INTERNAL_ERROR)
        }
    }

    /** Handle GET /api/editor-capabilities */
    private fun handleGetEditorCapabilities(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        return try {
            val opModeType =
                session.parameters["opModeType"]?.get(0)
                    ?: return createErrorResponse(
                        "Missing opModeType",
                        NanoHTTPD.Response.Status.NOT_FOUND,
                    )
            val robotId =
                session.parameters["robotId"]?.get(0)
                    ?: return createErrorResponse(
                        "Missing robotId",
                        NanoHTTPD.Response.Status.NOT_FOUND,
                    )

            val robot =
                MetadataService.getRobotById(robotId)
                    ?: return createErrorResponse(
                        "Robot not found",
                        NanoHTTPD.Response.Status.NOT_FOUND,
                    )
            val events = MetadataService.getEvents().filter { it.opModeType == opModeType }

            createJsonResponse(gson.toJson(Capabilities(robot.actions, events)))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting editor capabilities", e)
            createErrorResponse(
                "Error getting editor capabilities: ${e.message}",
                NanoHTTPD.Response.Status.INTERNAL_ERROR,
            )
        }
    }

    /** Handle GET /api/actions?id=:id */
    private fun handleGetAction(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        return try {
            val actionId =
                session.getQueryParameter("id")
                    ?: return createErrorResponse(
                        "Missing id",
                        NanoHTTPD.Response.Status.BAD_REQUEST,
                    )
            val actions = MetadataService.getActions()

            val action =
                actions.find { it.id == actionId }
                    ?: return createErrorResponse(
                        "Action not found",
                        NanoHTTPD.Response.Status.NOT_FOUND,
                    )

            createJsonResponse(gson.toJson(action))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting action", e)
            createErrorResponse(
                "Error getting action: ${e.message}",
                NanoHTTPD.Response.Status.INTERNAL_ERROR,
            )
        }
    }

    /** Handle GET /api/events?id=:id */
    private fun handleGetEvent(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        return try {
            val eventId =
                session.getQueryParameter("id")
                    ?: return createErrorResponse(
                        "Missing id",
                        NanoHTTPD.Response.Status.BAD_REQUEST,
                    )
            val events = MetadataService.getEvents()

            val event =
                events.find { it.id == eventId }
                    ?: return createErrorResponse(
                        "Event not found",
                        NanoHTTPD.Response.Status.NOT_FOUND,
                    )

            createJsonResponse(gson.toJson(event))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting event", e)
            createErrorResponse(
                "Error getting event: ${e.message}",
                NanoHTTPD.Response.Status.INTERNAL_ERROR,
            )
        }
    }

    /** Handle POST /api/validate */
    private fun handleValidate(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        try {
            val flowGraph =
                parseRequestBody<FlowGraph>(session)
                    ?: return createErrorResponse(
                        "Invalid request body",
                        NanoHTTPD.Response.Status.BAD_REQUEST,
                    )
            val validationResult = validateFlowGraph(flowGraph, null)

            return if (validationResult.isValid) {
                createJsonResponse(gson.toJson(mapOf("valid" to true)))
            } else {
                createJsonResponse(
                    gson.toJson(
                        mapOf(
                            "valid" to false,
                            "errors" to validationResult.errors,
                            "warnings" to validationResult.warnings,
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error validating flow graph", e)
            return createJsonResponse(
                gson.toJson(
                    mapOf("valid" to false, "errors" to listOf(e.message ?: "Unknown error"))
                )
            )
        }
    }

    /** Handle POST /api/generate */
    private fun handleGenerateCode(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        try {
            val flowGraph =
                parseRequestBody<FlowGraph>(session)
                    ?: return createErrorResponse(
                        "Invalid request body",
                        NanoHTTPD.Response.Status.BAD_REQUEST,
                    )
            val opModeId = session.parameters["id"]?.firstOrNull() ?: ""
            val opMode =
                if (opModeId.isNotEmpty()) opModes[opModeId]
                else
                    return createErrorResponse(
                        "Missing OpMode ID",
                        NanoHTTPD.Response.Status.BAD_REQUEST,
                    )
            if (opMode == null) {
                return createErrorResponse("OpMode not found", NanoHTTPD.Response.Status.NOT_FOUND)
            }

            val validationResult = validateFlowGraph(flowGraph, opMode.type)
            if (!validationResult.isValid) {
                return createJsonResponse(
                    gson.toJson(mapOf("success" to false, "errors" to validationResult.errors))
                )
            }
            val robotMeta = MetadataService.getRobotById(opMode.robotId)
            val generatedCode = CodeGenerator(flowGraph, opMode, robotMeta).generate()
            opModes[opMode.id] = opMode.copy(generatedCode = generatedCode)
            return createJsonResponse(gson.toJson(mapOf("code" to generatedCode)))
        } catch (e: Exception) {
            Log.e(TAG, "Error generating code", e)
            return createErrorResponse(
                "Error generating code: ${e.message}",
                NanoHTTPD.Response.Status.BAD_REQUEST,
            )
        }
    }

    /** Parse the request body */
    private inline fun <reified T> parseRequestBody(session: NanoHTTPD.IHTTPSession): T? {
        val contentLength = session.headers["content-length"]?.toIntOrNull() ?: 0
        if (contentLength == 0) return null

        val buffer = ByteArray(contentLength)

        try {
            session.inputStream.read(buffer, 0, contentLength)
            val json = String(buffer)

            // For complex types with generic collections
            if (T::class.java == FlowGraph::class.java) {
                return gson.fromJson(json, FlowGraph::class.java) as T?
            }

            return gson.fromJson(json, T::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing request body", e)
            return null
        }
    }

    /** Create a JSON response */
    private fun createJsonResponse(json: String): NanoHTTPD.Response {
        return NanoHTTPD.newFixedLengthResponse(
            NanoHTTPD.Response.Status.OK,
            "application/json",
            json,
        )
    }

    /** Create an error response */
    private fun createErrorResponse(
        message: String,
        status: NanoHTTPD.Response.Status,
    ): NanoHTTPD.Response {
        Log.e(TAG, message)
        return NanoHTTPD.newFixedLengthResponse(
            status,
            "application/json",
            gson.toJson(mapOf("error" to message)),
        )
    }

    /** Validate a flow graph */
    private fun validateFlowGraph(flowGraph: FlowGraph, opModeType: String?): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // Mode-specific validation
        if (opModeType == "ManualMode") {
            val events = flowGraph.nodes.count { it.type == "Event" }
            if (events == 0) errors.add("Flow must have at least one event node")

            if (flowGraph.nodes.any { it.type == "Event" && it.id == "" }) {
                errors.add("Manual flow should not have Start/End nodes")
            }
        } else {
            val startNodes = flowGraph.nodes.count { it.type == "start" }
            if (startNodes != 1) {
                errors.add("Autonomous flow must have at least one start node, found $startNodes")
            }
        }

        // Check for cycles (only relevant for action chains, not across triggers)
        try {
            checkForCycles(flowGraph)
        } catch (e: CycleDetectedException) {
            errors.add(e.message ?: "Cycle detected in flow graph")
        }

        // Validate connections
        val nodeIds = flowGraph.nodes.map { it.id }.toSet()
        flowGraph.connections.forEach { connection ->
            if (connection.sourceNode !in nodeIds) {
                errors.add(
                    "Connection refers to non-existent source node: ${connection.sourceNode}"
                )
            }
            if (connection.targetNode !in nodeIds) {
                errors.add(
                    "Connection refers to non-existent target node: ${connection.targetNode}"
                )
            }
        }

        return ValidationResult(errors, warnings)
    }

    private fun checkForCycles(flowGraph: FlowGraph) {
        val visited = mutableSetOf<String>()
        val visiting = mutableSetOf<String>()

        flowGraph.nodes.forEach { node ->
            if (
                node.type == "start" ||
                    node.type.endsWith("_trigger") ||
                    node.type == "while_pressed"
            ) {
                visitNode(node.id, flowGraph, visited, visiting)
            }
        }
    }

    private fun visitNode(
        nodeId: String,
        flowGraph: FlowGraph,
        visited: MutableSet<String>,
        visiting: MutableSet<String>,
    ) {
        if (nodeId in visiting) {
            throw CycleDetectedException("Cycle detected involving node: $nodeId")
        }

        if (nodeId in visited) {
            return
        }

        visiting.add(nodeId)

        val outgoingConnections = flowGraph.connections.filter { it.sourceNode == nodeId }

        outgoingConnections.forEach { connection ->
            visitNode(connection.targetNode, flowGraph, visited, visiting)
        }

        visiting.remove(nodeId)
        visited.add(nodeId)
    }

    /** Exception thrown when a cycle is detected */
    private class CycleDetectedException(message: String) : Exception(message)

    private fun findReachableNodes(
        startId: String,
        outgoing: Map<String, List<Connection>>,
    ): Set<String> {
        val visited = mutableSetOf<String>()
        val queue = ArrayDeque<String>()
        queue.add(startId)
        while (queue.isNotEmpty()) {
            val id = queue.removeFirst()
            if (id in visited) continue
            visited.add(id)
            outgoing[id]?.forEach { queue.add(it.targetNode) }
        }
        return visited
    }

    private fun NanoHTTPD.IHTTPSession.getQueryParameter(name: String): String? {
        return parameters[name]?.firstOrNull()?.takeIf { it.isNotBlank() }
    }
}
