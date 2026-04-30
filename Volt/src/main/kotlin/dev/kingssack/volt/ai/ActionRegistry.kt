package dev.kingssack.volt.ai

import com.acmerobotics.roadrunner.Action
import dev.kingssack.volt.annotations.AIParam
import dev.kingssack.volt.annotations.VoltAction
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters

/**
 * Registry for actions that can be executed by AI clients.
 *
 * Actions are registered via reflection by scanning instances for methods annotated with
 * [VoltAction]. The registry provides thread-safe access to registered actions and handles
 * parameter validation and type conversion when executing actions.
 */
object ActionRegistry {
    private val actions = ConcurrentHashMap<String, RegisteredAction>()

    /**
     * Represents a registered action with its metadata, owning instance, and function reference.
     *
     * @property descriptor the action's metadata including name, description, and parameters
     * @property instance the object instance on which the function will be called
     * @property function the Kotlin function reference to invoke
     */
    data class RegisteredAction(
        val descriptor: ActionDescriptor,
        val instance: Any,
        val function: KFunction<*>,
    )

    /** Exception thrown when action parameter validation fails. */
    class ParameterValidationException(message: String) : Exception(message)

    /**
     * Registers all [VoltAction]-annotated methods from the given instance.
     *
     * Methods must have `enableAITool = true` in their annotation to be registered. Each registered
     * action is assigned a unique ID based on the class qualified name and method name.
     *
     * @param instance the object instance to scan for actions
     */
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
                        val isNullable = parameter.type.isMarkedNullable
                        ActionParameter(
                            name = parameter.name ?: return@mapNotNull null,
                            type = parameter.type.toString(),
                            description = paramAnnotation?.description ?: "",
                            required = !isNullable,
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

    /**
     * Executes a registered action with the given parameters.
     *
     * Validates that all required parameters are present and that numeric parameters fall within
     * their specified min/max bounds.
     *
     * @param actionId the unique identifier of the action to execute
     * @param params a map of parameter names to values
     * @return the resulting [Action], or null if the action is not found
     * @throws ParameterValidationException if required parameters are missing or validation fails
     */
    fun execute(actionId: String, params: Map<String, Any?>): Action? {
        val registered = actions[actionId] ?: return null

        val args =
            registered.function.valueParameters.map { parameter ->
                val name =
                    parameter.name ?: throw ParameterValidationException("Parameter name is null")

                val descriptor = registered.descriptor.parameters.find { it.name == name }
                val value = params[name]

                // Check required parameters
                if (value == null) {
                    if (descriptor?.required != false) {
                        throw ParameterValidationException("Missing required parameter: $name")
                    }
                    return@map null
                }

                val converted =
                    convertParameter(value, parameter.type.toString())
                        ?: throw ParameterValidationException(
                            "Failed to convert parameter '$name' to ${parameter.type}"
                        )

                // Validate min/max bounds for numeric types
                if (converted is Number && descriptor != null) {
                    val numValue = converted.toDouble()
                    descriptor.min?.let { min ->
                        if (min != Double.NEGATIVE_INFINITY && numValue < min) {
                            throw ParameterValidationException(
                                "Parameter '$name' value $numValue is below minimum: $min"
                            )
                        }
                    }
                    descriptor.max?.let { max ->
                        if (max != Double.POSITIVE_INFINITY && numValue > max) {
                            throw ParameterValidationException(
                                "Parameter '$name' value $numValue is above maximum: $max"
                            )
                        }
                    }
                }

                converted
            }

        return registered.function.call(registered.instance, *args.toTypedArray()) as? Action
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

    private fun convertParameter(value: Any?, targetType: String): Any? =
        when {
            value == null -> null
            targetType.contains("Double") -> (value as? Number)?.toDouble()
            targetType.contains("Int") -> (value as? Number)?.toInt()
            targetType.contains("Boolean") -> value as? Boolean
            else -> value.toString()
        }

    private fun buildInputSchema(params: List<ActionParameter>): Map<String, Any> {
        val properties = params.associate { parameter ->
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
