package dev.kingssack.volt.ai

import com.acmerobotics.roadrunner.Action

object ActionRegistry {
    private val actions = mutableMapOf<String, RegisteredAction>()

    data class RegisteredAction(
        val descriptor: ActionDescriptor,
        val factory: (Map<String, Any>) -> Action,
    )

    fun register(descriptor: ActionDescriptor, factory: (Map<String, Any>) -> Action) {
        actions[descriptor.id] = RegisteredAction(descriptor, factory)
    }

    fun toAITools(): List<AITool> = actions.values.map { registered ->
        AITool(
            name = registered.descriptor.id,
            description = registered.descriptor.description,
            inputSchema = buildInputSchema(registered.descriptor.parameters)
        )
    }

    private fun buildInputSchema(params: List<ActionParameter>): Map<String, Any> {
        val properties = params.associate { parameter ->
            parameter.name to mapOf(
                "type" to mapKotlinTypeToJsonSchema(parameter.type),
                "description" to parameter.description,
            )
        }
        return mapOf(
            "type" to "object",
            "properties" to properties,
            "required" to params.filter { it.required }.map { it.name },
        )
    }

    private fun mapKotlinTypeToJsonSchema(type: String): String = when (type) {
        "Double", "Float" -> "number"
        "Int", "Long" -> "integer"
        "Boolean" -> "boolean"
        else -> "string"
    }
}
