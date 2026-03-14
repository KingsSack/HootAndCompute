package dev.kingssack.volt.web

import dev.kingssack.volt.model.RobotMetadata
import dev.kingssack.volt.service.MetadataService

class CodeGenerator(
    private val flowGraph: FlowGraph,
    private val opMode: OpModeDefinition,
    private val robotMeta: RobotMetadata?,
) {
    private val nodeMap: Map<String, Node> = flowGraph.nodes.associateBy { it.id }
    private val outgoing: Map<String, List<Connection>> =
        flowGraph.connections.groupBy { it.sourceNode }
    private val isManual = opMode.type == "ManualMode"

    fun generate(): String {
        val className = sanitizeClassName(opMode.name)

        return buildString {
                appendLine("package org.firstinspires.ftc.teamcode.generated")
                appendLine()

                generateImports().forEach { appendLine(it) }
                appendLine()

                appendLine(generateAnnotation())

                appendLine("${generateClassDeclaration(className)} {")
                appendLine("    override fun defineEvents() {")
                append(generateDefineEventsBody())
                appendLine()
                appendLine("    }")
                appendLine("}")
            }
            .trimEnd()
    }

    private fun sanitizeClassName(rawName: String): String {
        val cleaned =
            rawName.replace(Regex("[^A-Za-z0-9]"), "").split(" ").joinToString("") { word ->
                word.replaceFirstChar { it.uppercaseChar() }
            }

        return when {
            cleaned.isEmpty() -> "GeneratedOpMode"
            cleaned.first().isDigit() -> "_$cleaned"
            else -> cleaned
        }
    }

    private fun generateImports(): List<String> {
        val imports = mutableListOf<String>()

        val annotationClass = if (isManual) "TeleOp" else "Autonomous"
        imports.add("import com.qualcomm.robotcore.eventloop.opmode.$annotationClass")

        val subpackage = if (isManual) "manual" else "autonomous"
        imports.add("import dev.kingssack.volt.opmode.$subpackage.${opMode.type}")

        imports.add("import ${robotMeta?.id ?: "dev.kingssack.volt.robot.Robot"}")

        val eventFamily = if (isManual) "ManualEvent" else "AutonomousEvent"
        imports.add("import dev.kingssack.volt.util.Event.$eventFamily.*")

        if (isManual) {
            imports.add("import dev.kingssack.volt.util.buttons.AnalogInput")
            imports.add("import dev.kingssack.volt.util.buttons.Button")
        }

        return imports
    }

    private fun generateAnnotation(): String {
        val type = if (isManual) "TeleOp" else "Autonomous"
        return "@$type(name = \"${opMode.name}\", group = \"Volt\")"
    }

    private fun generateClassDeclaration(className: String): String {
        val typeSignature = robotMeta?.typeSignature ?: opMode.robotId
        val factoryExp = robotMeta?.factoryExpression ?: "${opMode.robotId}(it)"
        return "class $className : ${opMode.type}<$typeSignature>({ $factoryExp })"
    }

    private fun generateDefineEventsBody(): String {
        val eventNodes = flowGraph.nodes.filter { it.type == "Event" }

        if (eventNodes.isEmpty()) return "        // No events defined"

        return buildString {
            eventNodes.forEachIndexed { index, eventNode ->
                val actionChain = getOrderedActionChain(eventNode.id)
                val eventExpr = generateEventConstructor(eventNode)
                val isAnalog = eventNode.label in setOf("Change", "Threshold")

                if (isAnalog) appendLine("        $eventExpr then { value ->")
                else appendLine("        $eventExpr then {")
                if (actionChain.isEmpty()) {
                    appendLine("            // No actions connected")
                } else {
                    actionChain.forEach { actionNode ->
                        appendLine("            ${generateActionCall(actionNode)}")
                    }
                }
                appendLine("        }")

                if (index < eventNodes.size - 1) appendLine()
            }
        }
    }

    private fun getOrderedActionChain(eventNodeId: String): List<Node> {
        val result = mutableListOf<Node>()
        val visited = mutableSetOf<String>()
        val queue = ArrayDeque<String>()

        queue.add(eventNodeId)
        visited.add(eventNodeId)

        while (queue.isNotEmpty()) {
            val currentId = queue.removeFirst()
            val node = nodeMap[currentId]

            if (node != null && node.type == "Action") result.add(node)

            outgoing[currentId]?.forEach { connection ->
                if (connection.targetNode !in visited) {
                    visited.add(connection.targetNode)
                    queue.add(connection.targetNode)
                }
            }
        }

        return result
    }

    private fun generateEventConstructor(eventNode: Node): String =
        when (eventNode.label) {
            "Start" -> "Start"
            "Tap" -> {
                val button = eventNode.parameters["button"] ?: "A1"
                "Tap(Button.$button)"
            }
            "Release" -> {
                val button = eventNode.parameters["button"] ?: "A1"
                "Release(Button.$button)"
            }
            "Hold" -> {
                val button = eventNode.parameters["button"] ?: "A1"
                val duration = eventNode.parameters["durationMs"] ?: "200.0"
                "Hold(Button.$button, $duration)"
            }
            "DoubleTap" -> {
                val button = eventNode.parameters["button"] ?: "A1"
                "DoubleTap(Button.$button)"
            }
            "Change" -> {
                val input = eventNode.parameters["input"] ?: "A1"
                "Change(AnalogInput.$input)"
            }
            "Threshold" -> {
                val input = eventNode.parameters["input"] ?: "A1"
                val min = eventNode.parameters["min"] ?: "0.3"
                "Threshold(AnalogInput.$input, $min)"
            }
            "Combo" -> {
                val buttonsRaw = eventNode.parameters["buttons"]?.toString() ?: ""
                val buttons = buttonsRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                if (buttons.isEmpty()) "combo()"
                else "combo(${buttons.joinToString(", ") { "Button.$it" }})"
            }
            else -> "// Unsupported event: ${eventNode.label}"
        }

    private fun generateActionCall(actionNode: Node): String {
        val actionMeta =
            MetadataService.getActions().find { it.id == actionNode.actionId }
                ?: return "// Unknown action: ${actionNode.actionId}"

        val args =
            actionMeta.parameters.mapNotNull { paramMeta ->
                val rawValue = actionNode.parameters[paramMeta.name]

                when {
                    rawValue != null && rawValue.toString().isNotBlank() ->
                        formatParameterValue(rawValue, paramMeta.type)
                    paramMeta.defaultValue != null -> null
                    else -> null
                }
            }

        val argStr = args.joinToString(", ")
        return "+robot.${actionMeta.accessPath}($argStr)"
    }

    private fun formatParameterValue(value: Any?, type: String): String {
        if (value == null) return "null"
        val str = value.toString()
        return when (type) {
            "Double" -> str
            "Float" -> "${str}f"
            "Int" -> str.toDoubleOrNull()?.toInt()?.toString() ?: str
            "Long" -> "${str}L"
            "Boolean" -> str.lowercase()
            "String" -> "\"$str\""
            "Button" -> "Button.$str"
            "AnalogInput" -> "AnalogInput.$str"
            else -> str
        }
    }
}
