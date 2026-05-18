package dev.kingssack.volt.ai

import com.acmerobotics.roadrunner.Action
import dev.kingssack.volt.annotations.AIParam
import dev.kingssack.volt.annotations.VoltAction
import kotlinx.serialization.json.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters

/**
 * Registry for actions that can be executed by AI clients.
 *
 * Actions are registered via reflection by scanning instances for methods annotated with
 * [VoltAction]. The registry provides thread-safe access to registered actions and handles
 * parameter validation and type conversion when executing actions.
 */
class ActionRegistry(private val providers: List<Any>) {
    val actions: Map<String, AITool> = buildMap {
        providers.forEach { provider ->
            provider::class
                .memberFunctions
                .filter { it.findAnnotation<VoltAction>() != null }
                .forEach { function ->
                    val annotation = function.findAnnotation<VoltAction>()!!

                    if (!annotation.enableAITool) return@forEach

                    val schema = buildInputSchema(function)

                    val definition =
                        AITool(
                            name = annotation.name,
                            description = annotation.description,
                            parameters = schema,
                            invoke = { args -> callFn(provider, function, args) },
                        )

                    put(annotation.name, definition)
                }
        }
    }

    /** Exception thrown when action parameter validation fails. */
    class ParameterValidationException(message: String) : Exception(message)

    private fun callFn(instance: Any, function: KFunction<*>, args: Map<String, Any?>): Action {
        val paramMap =
            function.valueParameters.associateWith { parameter ->
                val raw = args[parameter.name]
                coerce(raw, parameter.type)
            }
        return function.callBy(mapOf(function.instanceParameter!! to instance) + paramMap) as Action
    }

    private fun coerce(value: Any?, target: KType): Any? {
        if (value == null) return null
        val classifier = target.classifier
        if (classifier is KClass<*> && classifier.java.isEnum) {
            return classifier.java.enumConstants?.find { it.toString() == value.toString() }
        }
        return when (target.classifier) {
            Int::class -> (value as Number).toInt()
            Long::class -> (value as Number).toLong()
            Double::class -> (value as Number).toDouble()
            Boolean::class -> value as Boolean
            else -> value.toString()
        }
    }

    private fun buildInputSchema(function: KFunction<*>): JsonObject {
        val required = mutableListOf<String>()
        val properties = buildJsonObject {
            for (parameter in function.valueParameters) {
                val meta = parameter.findAnnotation<AIParam>() ?: continue
                val name = parameter.name!!
                val prop = buildJsonObject {
                    put("type", kotlinTypeToJsonType(parameter.type))
                    put("description", meta.description)
                    if (meta.enum.isNotEmpty()) {
                        putJsonArray("enum") { meta.enum.forEach { add(it) } }
                    }
                }

                put(name, prop)
                if (meta.required) required += name
            }
        }
        return buildJsonObject {
            put("type", "object")
            put("properties", properties)
            putJsonArray("required") { required.forEach { add(it) } }
        }
    }

    private fun kotlinTypeToJsonType(type: KType): String =
        when (type.classifier) {
            String::class -> "string"
            Int::class,
            Long::class -> "integer"
            Float::class,
            Double::class -> "number"
            Boolean::class -> "boolean"
            else -> "string"
        }
}
