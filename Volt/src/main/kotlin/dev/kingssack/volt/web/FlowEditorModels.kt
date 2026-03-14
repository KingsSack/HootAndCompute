package dev.kingssack.volt.web

import dev.kingssack.volt.model.ActionMetadata
import dev.kingssack.volt.model.EventMetadata
import java.util.UUID

data class OpModeDefinition(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: String, // "AutonomousMode" or "ManualMode"
    val robotId: String,
    val flowGraph: FlowGraph,
    val generatedCode: String? = null,
    val constructorParams: Map<String, Any?> = emptyMap(),
)

data class FlowGraph(val nodes: List<Node>, val connections: List<Connection>)

data class Node(
    val id: String,
    val label: String,
    val type: String,
    val actionId: String? = null,
    val eventId: String? = null,
    val parameters: Map<String, Any?> = emptyMap(),
    val position: Position,
    val ports: Ports,
)

data class Connection(
    val id: String,
    val sourceNode: String,
    val sourcePort: String,
    val targetNode: String,
    val targetPort: String,
)

data class Position(val x: Double, val y: Double)

data class Ports(val inputs: List<String>, val outputs: List<String>)

data class Capabilities(val actions: List<ActionMetadata>, val events: List<EventMetadata>)

data class OpModeCreationConfig(
    val name: String,
    val type: String,
    val robotId: String,
    val constructorParams: Map<String, Any?> = emptyMap(),
)

data class ValidationResult(val errors: List<String>, val warnings: List<String>) {
    val isValid: Boolean
        get() = errors.isEmpty()
}
