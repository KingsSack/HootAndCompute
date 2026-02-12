package dev.kingssack.volt.ai

import com.acmerobotics.roadrunner.Action
import dev.kingssack.volt.annotations.AIParam
import dev.kingssack.volt.annotations.VoltAction
import dev.kingssack.volt.service.MetadataService
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters

object ActionRegistry {
    private val actions = mutableMapOf<String, RegisteredAction>()

    data class RegisteredAction(
        val descriptor: ActionDescriptor,
        val instance: Any,
        val function: KFunction<*>,
    )

    fun registerInstance(instance: Any) {
        instance::class
            .memberFunctions
            .filter { it.findAnnotation<VoltAction>() != null }
            .forEach { function ->
                val annotation = function.findAnnotation<VoltAction>()!!

                if (!annotation.enableAITool) return@forEach

                val id = "${instance::class.qualifiedName}.${function.name}"

                val parameters =
                    function.valueParameters.mapNotNull { parameter ->
                        val paramAnnotation = parameter.findAnnotation<AIParam>()
                        ActionParameter(
                            name = parameter.name ?: return@mapNotNull null,
                            type = parameter.type.toString(),
                            description = paramAnnotation?.description ?: "",
                            min = paramAnnotation?.min,
                            max = paramAnnotation?.max,
                        )
                    }

                val descriptor =
                    ActionDescriptor(
                        id = id,
                        name = annotation.name,
                        description = annotation.description,
                        parameters = parameters,
                    )

                actions[descriptor.id] = RegisteredAction(descriptor, instance, function)
            }
    }

    fun execute(actionId: String, params: Map<String, Any?>): Action? {
        val registered = actions[actionId] ?: return null

        val args =
            registered.function.valueParameters
                .map { parameter ->
                    params[parameter.name]?.let { value ->
                        convertParameter(value, parameter.type.toString())
                    }
                }
                .toTypedArray()

        return registered.function.call(registered.instance, *args) as? Action
    }

    fun toAITools(): List<AITool> =
        actions.values.map { registered ->
            AITool(
                name = registered.descriptor.id,
                description = registered.descriptor.description,
                inputSchema = buildInputSchema(registered.descriptor.parameters),
            )
        }

    fun clear() = actions.clear()

    private fun convertParameter(value: Any?, targetType: String): Any? {
        return when {
            value == null -> null
            targetType.contains("Double") -> (value as? Number)?.toDouble()
            targetType.contains("Int") -> (value as? Number)?.toInt()
            targetType.contains("Boolean") -> value as? Boolean
            else -> value.toString()
        }
    }

    private fun buildInputSchema(params: List<ActionParameter>): Map<String, Any> {
        val properties =
            params.associate { parameter ->
                parameter.name to
                    buildMap {
                        put("type", mapKotlinTypeToJsonSchema(parameter.type))
                        put("description", parameter.description)
                        parameter.min?.let { put("minimum", it) }
                        parameter.max?.let { put("maximum", it) }
                    }
            }
        return mapOf(
            "type" to "object",
            "properties" to properties,
            "required" to params.filter { it.required }.map { it.name },
        )
    }

    private fun mapKotlinTypeToJsonSchema(type: String): String =
        when {
            type.contains("Double") || type.contains("Float") -> "number"
            type.contains("Int") || type.contains("Long") -> "integer"
            type.contains("Boolean") -> "boolean"
            else -> "string"
        }
}
