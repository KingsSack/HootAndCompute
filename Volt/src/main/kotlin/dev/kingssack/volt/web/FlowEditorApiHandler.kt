package dev.kingssack.volt.web

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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

    /** Configuration for an OpMode definition */
    data class OpModeDefinition(
        val id: String = UUID.randomUUID().toString(),
        val name: String,
        val type: String, // "AutonomousMode" or "ManualMode"
        val robotType: String,
        val version: String = "1.0",
        val lastModified: Long = System.currentTimeMillis(),
        val flowGraph: FlowGraph,
        val generatedCode: String? = null,
        val metadata: OpModeMetadata = OpModeMetadata(),
    )

    /** Flow graph structure containing nodes and connections */
    data class FlowGraph(val nodes: List<Node>, val connections: List<Connection>)

    /** Node structure in the flow graph */
    data class Node(
        val id: String,
        val type: String, // "start", "action", "control", "end"
        val actionClass: String? = null, // For action nodes
        val position: Position,
        val data: NodeData,
        val ports: Ports,
    )

    /** Connection structure in the flow graph */
    data class Connection(
        val id: String,
        val sourceNode: String,
        val sourcePort: String,
        val targetNode: String,
        val targetPort: String,
    )

    /** Position coordinates */
    data class Position(val x: Double, val y: Double)

    /** Node data containing label and parameters */
    data class NodeData(val label: String, val parameters: Map<String, Any?> = emptyMap())

    /** Ports definition for a node */
    data class Ports(val inputs: List<String>, val outputs: List<String>)

    /** OpMode metadata */
    data class OpModeMetadata(
        val author: String = "Unknown",
        val description: String = "",
        val tags: List<String> = emptyList(),
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getResponse(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val uri = session.uri.removePrefix("/volt")
        val method = session.method

        Log.d(TAG, "Handling request: $method $uri")

        return when ( // OpMode Management Endpoints
        method) {
            NanoHTTPD.Method.GET if uri == "/api/opmodes" -> handleGetOpModes()
            NanoHTTPD.Method.GET if uri.startsWith("/api/opmodes/") -> handleGetOpMode(uri)

            NanoHTTPD.Method.POST if uri == "/api/opmodes" -> handleCreateOpMode(session)
            NanoHTTPD.Method.PUT if uri.startsWith("/api/opmodes/") ->
                handleUpdateOpMode(uri, session)

            NanoHTTPD.Method.DELETE if uri.startsWith("/api/opmodes/") -> handleDeleteOpMode(uri)

            // Robot Discovery Endpoints
            NanoHTTPD.Method.GET if uri == "/api/robots" -> handleGetRobots()
            NanoHTTPD.Method.GET if uri.startsWith("/api/robots/") -> handleGetRobot(uri)

            // Action Discovery Endpoints
            NanoHTTPD.Method.GET if uri == "/api/actions" -> handleGetActions(session)
            NanoHTTPD.Method.GET if uri.startsWith("/api/actions/") -> handleGetAction(uri)

            // Validation Endpoint
            NanoHTTPD.Method.POST if uri == "/api/validate" -> handleValidate(session)

            // Code Generation Endpoint
            NanoHTTPD.Method.POST if uri == "/api/generate" -> handleGenerateCode(session)
            else -> createErrorResponse("Not found", NanoHTTPD.Response.Status.NOT_FOUND)
        }
    }

    // MARK: - OpMode Management

    /** Handle GET /api/opmodes */
    private fun handleGetOpModes(): NanoHTTPD.Response {
        val opModeList =
            opModes.values.map { opMode ->
                mapOf(
                    "id" to opMode.id,
                    "name" to opMode.name,
                    "type" to opMode.type,
                    "robotType" to opMode.robotType,
                    "version" to opMode.version,
                    "lastModified" to opMode.lastModified,
                )
            }
        return createJsonResponse(gson.toJson(opModeList))
    }

    /** Handle GET /api/opmodes/:id */
    private fun handleGetOpMode(uri: String): NanoHTTPD.Response {
        val id = uri.substringAfterLast("/")
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
        val requestBody =
            parseRequestBody<String>(session)
                ?: return createErrorResponse(
                    "Invalid request body",
                    NanoHTTPD.Response.Status.BAD_REQUEST,
                )

        try {
            val opModeConfig = gson.fromJson(requestBody, OpModeCreationConfig::class.java)
            val opModeId = UUID.randomUUID().toString()

            // Create a new OpMode with the default flow graph (just a Start node)
            val startNode =
                Node(
                    id = "start_node_${opModeId}",
                    type = "start",
                    position = Position(0.0, 0.0),
                    data = NodeData(label = "Start"),
                    ports = Ports(inputs = emptyList(), outputs = listOf("output_1")),
                )

            val flowGraph = FlowGraph(nodes = listOf(startNode), connections = emptyList())

            val opMode =
                OpModeDefinition(
                    id = opModeId,
                    name = opModeConfig.name,
                    type = opModeConfig.type,
                    robotType = opModeConfig.robotType,
                    flowGraph = flowGraph,
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

    /** Handle PUT /api/opmodes/:id */
    private fun handleUpdateOpMode(
        uri: String,
        session: NanoHTTPD.IHTTPSession,
    ): NanoHTTPD.Response {
        val id = uri.substringAfterLast("/")
        val requestBody =
            parseRequestBody<String>(session)
                ?: return createErrorResponse(
                    "Invalid request body",
                    NanoHTTPD.Response.Status.BAD_REQUEST,
                )

        try {
            val opModeUpdate =
                gson.fromJson(requestBody, OpModeDefinition::class.java).copy(id = id)

            // Update the OpMode
            opModes[id] = opModeUpdate.copy(lastModified = System.currentTimeMillis())

            return createJsonResponse(gson.toJson(mapOf("success" to true)))
        } catch (e: Exception) {
            Log.e(TAG, "Error updating OpMode", e)
            return createErrorResponse(
                "Error updating OpMode: ${e.message}",
                NanoHTTPD.Response.Status.BAD_REQUEST,
            )
        }
    }

    /** Handle DELETE /api/opmodes/:id */
    private fun handleDeleteOpMode(uri: String): NanoHTTPD.Response {
        val id = uri.substringAfterLast("/")
        if (opModes.remove(id) != null) {
            return createJsonResponse(gson.toJson(mapOf("success" to true)))
        }
        return createErrorResponse("OpMode not found", NanoHTTPD.Response.Status.NOT_FOUND)
    }

    // MARK: - Robot Discovery

    /** Handle GET /api/robots */
    private fun handleGetRobots(): NanoHTTPD.Response {
        return try {
            val (robots, _) = MetadataService.getAllMetadata()
            createJsonResponse(gson.toJson(robots))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting robots", e)
            createErrorResponse(
                "Error getting robots: ${e.message}",
                NanoHTTPD.Response.Status.INTERNAL_ERROR,
            )
        }
    }

    /** Handle GET /api/robots/:name */
    private fun handleGetRobot(uri: String): NanoHTTPD.Response {
        return try {
            val robotName = uri.substringAfterLast("/")
            val (robots, _) = MetadataService.getAllMetadata()

            val robot =
                robots.find { it.simpleName == robotName }
                    ?: return createErrorResponse(
                        "Robot not found",
                        NanoHTTPD.Response.Status.NOT_FOUND,
                    )

            // For now, just return the basic metadata
            // In a full implementation, we'd return detailed hardware configuration
            createJsonResponse(
                gson.toJson(mapOf("name" to robot.simpleName, "qualifiedName" to robot.qualifiedName))
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting robot", e)
            createErrorResponse(
                "Error getting robot: ${e.message}",
                NanoHTTPD.Response.Status.INTERNAL_ERROR,
            )
        }
    }

    /** Handle GET /api/actions */
    @Suppress("unused")
    private fun handleGetActions(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        return try {
            val (_, actions) = MetadataService.getAllMetadata()
            createJsonResponse(gson.toJson(actions))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting actions", e)
            createErrorResponse(
                "Error getting actions: ${e.message}",
                NanoHTTPD.Response.Status.INTERNAL_ERROR,
            )
        }
    }

    /** Handle GET /api/actions/:id */
    private fun handleGetAction(uri: String): NanoHTTPD.Response {
        return try {
            val actionId = uri.substringAfterLast("/")
            val (_, actions) = MetadataService.getAllMetadata()

            val action =
                actions.find { it.id == actionId }
                    ?: return createErrorResponse(
                        "Action not found",
                        NanoHTTPD.Response.Status.NOT_FOUND,
                    )

            // Return detailed metadata
            createJsonResponse(gson.toJson(action))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting action", e)
            createErrorResponse(
                "Error getting action: ${e.message}",
                NanoHTTPD.Response.Status.INTERNAL_ERROR,
            )
        }
    }

    /** Handle POST /api/validate */
    private fun handleValidate(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val requestBody =
            parseRequestBody<String>(session)
                ?: return createErrorResponse(
                    "Invalid request body",
                    NanoHTTPD.Response.Status.BAD_REQUEST,
                )

        try {
            val flowGraph = gson.fromJson(requestBody, FlowGraph::class.java)
            val validationResult = validateFlowGraph(flowGraph)

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
        val requestBody =
            parseRequestBody<String>(session)
                ?: return createErrorResponse(
                    "Invalid request body",
                    NanoHTTPD.Response.Status.BAD_REQUEST,
                )

        try {
            val flowGraph = gson.fromJson(requestBody, FlowGraph::class.java)
            val opModeId = session.parameters["id"]?.firstOrNull() ?: ""
            val opMode = if (opModeId.isNotEmpty()) opModes[opModeId] else null

            // Validate first
            val validationResult = validateFlowGraph(flowGraph)
            if (!validationResult.isValid) {
                return createJsonResponse(
                    gson.toJson(mapOf("success" to false, "errors" to validationResult.errors))
                )
            }

            // Generate code
            val generatedCode = generateCodeFromFlowGraph(flowGraph, opMode)

            // Update the OpMode with generated code if we have an ID
            if (opMode != null) {
                val updatedOpMode = opMode.copy(generatedCode = generatedCode)
                opModes[opMode.id] = updatedOpMode
            }

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
            session.inputStream.readNBytes(buffer, 0, contentLength)
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
    private fun validateFlowGraph(flowGraph: FlowGraph): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // Check that there's exactly one start node
        val startNodes = flowGraph.nodes.count { it.type == "start" }
        if (startNodes != 1) {
            errors.add("Flow graph must have exactly one start node, found $startNodes")
        }

        // Check for cycles
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

    /** Check for cycles in the flow graph */
    private fun checkForCycles(flowGraph: FlowGraph) {
        val visited = mutableSetOf<String>()
        val visiting = mutableSetOf<String>()

        flowGraph.nodes.forEach { node ->
            if (node.type == "start") {
                visitNode(node.id, flowGraph, visited, visiting)
            }
        }
    }

    /** Depth-first search to detect cycles */
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

        // Find outgoing connections
        val outgoingConnections = flowGraph.connections.filter { it.sourceNode == nodeId }

        outgoingConnections.forEach { connection ->
            visitNode(connection.targetNode, flowGraph, visited, visiting)
        }

        visiting.remove(nodeId)
        visited.add(nodeId)
    }

    /** Exception thrown when a cycle is detected */
    private class CycleDetectedException(message: String) : Exception(message)

    /** Generate Kotlin code from a flow graph */
    private fun generateCodeFromFlowGraph(flowGraph: FlowGraph, opMode: OpModeDefinition?): String {
        val builder = StringBuilder()

        // Start building the Kotlin class
        if (opMode != null) {
            builder.appendLine("package org.firstinspires.ftc.teamcode.generated")
            builder.appendLine()
            builder.appendLine("/**")
            builder.appendLine(" * Generated OpMode: ${opMode.name}")
            if (opMode.metadata.description.isNotEmpty()) {
                builder.appendLine(" * ${opMode.metadata.description}")
            }
            builder.appendLine(" */")

            // Choose the right annotation based on type
            val annotation =
                when (opMode.type) {
                    "AutonomousMode" -> "@Autonomous(name = \"${opMode.name}\", group = \"Volt\")"
                    "ManualMode" -> "@TeleOp(name = \"${opMode.name}\", group = \"Volt\")"
                    else -> "@OpMode(name = \"${opMode.name}\")"
                }
            builder.appendLine(annotation)

            // Build the class declaration
            builder.appendLine(
                "class ${opMode.name.replace(" ".toRegex(), "").replace("-".toRegex(), "_")} : ${opMode.type}<${opMode.robotType}>({ hardwareMap -> ${opMode.robotType}(hardwareMap) }) {"
            )
            builder.appendLine()
        }

        // Generate the sequence based on the flow graph
        if (flowGraph.nodes.isEmpty()) {
            builder.appendLine("    override fun runOpMode() {")
            builder.appendLine("        telemetry.addData(\"Status\", \"Ready\")")
            builder.appendLine("        telemetry.update()")
            builder.appendLine("    }")
            return builder.toString()
        }

        // Find start node and build execution flow
        val startNode = flowGraph.nodes.find { it.type == "start" }
        if (startNode != null) {
            // Build the execution sequence
            builder.appendLine("    override fun runOpMode() {")
            builder.appendLine("        telemetry.addData(\"Status\", \"Initializing\")")
            builder.appendLine("        telemetry.update()")

            // Traverse the flow graph starting from the start node, following connections
            builder.appendLine("        // Generated execution sequence")

            // Build a map of nodes by ID for quick lookup
            val nodeMap = flowGraph.nodes.associateBy { it.id }

            // Build a map of outgoing connections for each node
            val outgoingConnections = flowGraph.connections.groupBy { it.sourceNode }

            // Traverse the graph in execution order using BFS
            val visited = mutableSetOf<String>()
            val queue = ArrayDeque<String>()
            queue.add(startNode.id)

            while (queue.isNotEmpty()) {
                val currentNodeId = queue.removeFirst()

                if (currentNodeId in visited) continue
                visited.add(currentNodeId)

                val node = nodeMap[currentNodeId] ?: continue

                // Generate code for this node
                when (node.type) {
                    "action" -> {
                        if (node.actionClass != null) {
                            val actionName = node.data.label.replace(" ".toRegex(), "")
                            builder.appendLine("        // ${node.data.label}")
                            builder.appendLine("        execute { +robot.${actionName}() }")
                        }
                    }
                    "control" -> {
                        builder.appendLine("        // Control flow: ${node.data.label}")
                    }
                    else -> {}
                }

                // Add connected nodes to the queue (in order of their connections)
                outgoingConnections[currentNodeId]?.forEach { connection ->
                    if (connection.targetNode !in visited) {
                        queue.add(connection.targetNode)
                    }
                }
            }

            builder.appendLine()
            builder.appendLine("        telemetry.addData(\"Status\", \"Complete\")")
            builder.appendLine("        telemetry.update()")
            builder.appendLine("    }")
        }

        // Close the class
        if (opMode != null) {
            builder.appendLine("}")
        }

        return builder.toString()
    }

    // MARK: - Helper Classes

    /** OpMode creation configuration */
    data class OpModeCreationConfig(val name: String, val type: String, val robotType: String)

    /** Validation result */
    data class ValidationResult(val errors: List<String>, val warnings: List<String>) {
        val isValid: Boolean
            get() = errors.isEmpty()
    }
}
